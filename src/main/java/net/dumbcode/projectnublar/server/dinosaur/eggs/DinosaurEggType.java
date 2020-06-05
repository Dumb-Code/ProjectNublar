package net.dumbcode.projectnublar.server.dinosaur.eggs;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

@Getter
public class DinosaurEggType {
    public static final DinosaurEggType EMPTY = new DinosaurEggType(0F, 0F, TextureMap.LOCATION_MISSING_TEXTURE, TextureMap.LOCATION_MISSING_TEXTURE);

    private final float eggLength; //Used for incubator
    private final float scale; //Used for incubator
    private final ResourceLocation modelLocation;
    private final ResourceLocation[] texture;

    @Getter(AccessLevel.NONE)
    private ResourceLocation cachedTexture;

    private TabulaModel eggModel;

    public DinosaurEggType(float eggLength, float scale, ResourceLocation modelLocation, ResourceLocation... texture) {
        this.eggLength = eggLength;
        this.scale = scale;
        this.modelLocation = modelLocation;
        this.texture = texture;
    }

    public TabulaModel getEggModel() {
        if(this.eggModel != null) {
            return this.eggModel;
        }
        if(TextureManager.RESOURCE_LOCATION_EMPTY.equals(this.modelLocation)) {
            return this.eggModel = ModelMissing.INSTANCE;
        }
        return this.eggModel = TabulaUtils.getModel(this.modelLocation);
    }

    public void clearCache() {
        this.eggModel = null;
    }

    public ResourceLocation getTexture() {
        if(this.cachedTexture == null) {
            this.cachedTexture = TextureUtils.generateMultipleTexture(this.texture);
        }
        return this.cachedTexture;
    }

    public static NBTTagCompound writeToNBT(DinosaurEggType type) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("length", type.eggLength);
        nbt.setFloat("scale", type.scale);
        nbt.setString("model_location", type.modelLocation.toString());
        nbt.setTag("textures", Arrays.stream(type.texture).map(r -> new NBTTagString(r.toString())).collect(CollectorUtils.toNBTTagList()));
        return nbt;
    }

    public static DinosaurEggType readFromNBT(NBTTagCompound nbt) {
        return new DinosaurEggType(
            nbt.getFloat("length"),
            nbt.getFloat("scale"),
            new ResourceLocation(nbt.getString("model_location")),
            StreamUtils.stream(nbt.getTagList("textures", Constants.NBT.TAG_STRING)).map(b -> new ResourceLocation(((NBTTagString)b).getString())).toArray(ResourceLocation[]::new)
        );
    }

    public static JsonObject writeToJson(DinosaurEggType type) {
        JsonObject json = new JsonObject();
        json.addProperty("length", type.eggLength);
        json.addProperty("scale", type.scale);
        json.addProperty("model_location", type.modelLocation.toString());
        json.add("textures", Arrays.stream(type.texture).map(ResourceLocation::toString).collect(CollectorUtils.toJsonArrayString()));
        return json;
    }

    public static DinosaurEggType readFromJson(JsonObject json) {
        return new DinosaurEggType(
            JsonUtils.getFloat(json, "length"),
            JsonUtils.getFloat(json, "scale"),
            new ResourceLocation(JsonUtils.getString(json, "model_location")),
            StreamUtils.stream(JsonUtils.getJsonArray(json, "textures")).map(e -> new ResourceLocation(e.getAsString())).toArray(ResourceLocation[]::new)
        );
    }

    public static void writeToBuf(DinosaurEggType type, ByteBuf buf) {
        buf.writeFloat(type.eggLength);
        buf.writeFloat(type.scale);
        ByteBufUtils.writeUTF8String(buf, type.modelLocation.toString());

        buf.writeByte(type.texture.length);
        Arrays.stream(type.texture).forEachOrdered(r -> ByteBufUtils.writeUTF8String(buf, r.toString()));
    }

    public static DinosaurEggType readFromBuf(ByteBuf buf) {
        return new DinosaurEggType(
            buf.readFloat(),
            buf.readFloat(),
            new ResourceLocation(ByteBufUtils.readUTF8String(buf)),
            IntStream.range(0, buf.readByte()).mapToObj(i -> new ResourceLocation(ByteBufUtils.readUTF8String(buf))).toArray(ResourceLocation[]::new)
        );
    }
}
