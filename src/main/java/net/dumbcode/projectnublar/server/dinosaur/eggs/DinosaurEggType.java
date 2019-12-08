package net.dumbcode.projectnublar.server.dinosaur.eggs;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

@Getter
@RequiredArgsConstructor
public class DinosaurEggType {
    public static final DinosaurEggType EMPTY = new DinosaurEggType(0F, 0F, TextureMap.LOCATION_MISSING_TEXTURE, TextureMap.LOCATION_MISSING_TEXTURE);

    private final float eggLength; //Used for incubator
    private final float scale; //Used for incubator
    private final ResourceLocation texture;
    private final ResourceLocation modelLocation;

    private TabulaModel eggModel;

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

    public static NBTTagCompound writeToNBT(DinosaurEggType type) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("length", type.eggLength);
        nbt.setFloat("scale", type.scale);
        nbt.setString("texture", type.texture.toString());
        nbt.setString("model_location", type.modelLocation.toString());
        return nbt;
    }

    public static DinosaurEggType readFromNBT(NBTTagCompound nbt) {
        return new DinosaurEggType(
            nbt.getFloat("length"),
            nbt.getFloat("scale"),
            new ResourceLocation(nbt.getString("texture")),
            new ResourceLocation(nbt.getString("model_location"))
        );
    }

    public static JsonObject writeToJson(DinosaurEggType type) {
        JsonObject json = new JsonObject();
        json.addProperty("length", type.eggLength);
        json.addProperty("scale", type.scale);
        json.addProperty("texture", type.texture.toString());
        json.addProperty("model_location", type.modelLocation.toString());
        return json;
    }

    public static DinosaurEggType readFromJson(JsonObject json) {
        return new DinosaurEggType(
            JsonUtils.getFloat(json, "length"),
            JsonUtils.getFloat(json, "scale"),
            new ResourceLocation(JsonUtils.getString(json, "texture")),
            new ResourceLocation(JsonUtils.getString(json, "model_location"))
        );
    }

    public static void writeToBuf(DinosaurEggType type, ByteBuf buf) {
        buf.writeFloat(type.eggLength);
        buf.writeFloat(type.scale);
        ByteBufUtils.writeUTF8String(buf, type.texture.toString());
        ByteBufUtils.writeUTF8String(buf, type.modelLocation.toString());
    }

    public static DinosaurEggType readFromBuf(ByteBuf buf) {
        return new DinosaurEggType(
            buf.readFloat(),
            buf.readFloat(),
            new ResourceLocation(ByteBufUtils.readUTF8String(buf)),
            new ResourceLocation(ByteBufUtils.readUTF8String(buf))
        );
    }
}
