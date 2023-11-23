package net.dumbcode.projectnublar.server.dinosaur.eggs;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.stream.IntStream;

@Getter
public class DinosaurEggType {
    public static final DinosaurEggType EMPTY = new DinosaurEggType(0F, 0F, new ResourceLocation("missingno"), new ResourceLocation("missingno"));

    private final float eggLength; //Used for incubator
    private final float scale; //Used for incubator
    private final ResourceLocation modelLocation;
    private final ResourceLocation[] texture;

    @Getter(AccessLevel.NONE)
    private ResourceLocation cachedTexture;

    @OnlyIn(Dist.CLIENT)
    private DCMModel eggModel;

    public DinosaurEggType(float eggLength, float scale, ResourceLocation modelLocation, ResourceLocation... texture) {
        this.eggLength = eggLength;
        this.scale = scale;
        this.modelLocation = modelLocation;
        this.texture = texture;
    }

    @OnlyIn(Dist.CLIENT)
    public DCMModel getEggModel() {
        if(this.eggModel != null) {
            return this.eggModel;
        }
        if(TextureManager.INTENTIONAL_MISSING_TEXTURE.equals(this.modelLocation)) {
            return this.eggModel = ModelMissing.getInstance();
        }
        return this.eggModel = DCMUtils.getModel(this.modelLocation);
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

    public static CompoundTag writeToNBT(DinosaurEggType type) {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("length", type.eggLength);
        nbt.putFloat("scale", type.scale);
        nbt.putString("model_location", type.modelLocation.toString());
        nbt.put("textures", Arrays.stream(type.texture).map(r -> StringNBT.valueOf(r.toString())).collect(CollectorUtils.toNBTTagList()));
        return nbt;
    }

    public static DinosaurEggType readFromNBT(CompoundTag nbt) {
        return new DinosaurEggType(
            nbt.getFloat("length"),
            nbt.getFloat("scale"),
            new ResourceLocation(nbt.getString("model_location")),
            StreamUtils.stream(nbt.getList("textures", Constants.NBT.TAG_STRING)).map(b -> new ResourceLocation(b.getAsString())).toArray(ResourceLocation[]::new)
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
            JSONUtils.getAsFloat(json, "length"),
            JSONUtils.getAsFloat(json, "scale"),
            new ResourceLocation(JSONUtils.getAsString(json, "model_location")),
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "textures")).map(e -> new ResourceLocation(e.getAsString())).toArray(ResourceLocation[]::new)
        );
    }

    public static void writeToBuf(DinosaurEggType type, FriendlyByteBuf buf) {
        buf.writeFloat(type.eggLength);
        buf.writeFloat(type.scale);
        buf.writeUtf(type.modelLocation.toString());

        buf.writeByte(type.texture.length);
        Arrays.stream(type.texture).forEachOrdered(r -> buf.writeUtf(r.toString()));
    }

    public static DinosaurEggType readFromBuf(FriendlyByteBuf buf) {
        return new DinosaurEggType(
            buf.readFloat(),
            buf.readFloat(),
            new ResourceLocation(buf.readUtf()),
            IntStream.range(0, buf.readByte()).mapToObj(i -> new ResourceLocation(buf.readUtf())).toArray(ResourceLocation[]::new)
        );
    }
}
