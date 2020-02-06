package net.dumbcode.projectnublar.server.utils;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.Random;

@AllArgsConstructor
public class GaussianValue {
    private final float mean;
    private final float deviation;

    public float getRandomValue(Random random) {
        return (float) (this.mean + random.nextGaussian() * this.deviation);
    }

    public static NBTTagCompound writeToNBT(GaussianValue value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("mean", value.mean);
        nbt.setFloat("deviation", value.deviation);
        return nbt;
    }

    public static GaussianValue readFromNBT(NBTTagCompound nbt) {
        return new GaussianValue(nbt.getFloat("mean"), nbt.getFloat("deviation"));
    }

    public static JsonObject writeToJson(GaussianValue value) {
        JsonObject json = new JsonObject();
        json.addProperty("mean", value.mean);
        json.addProperty("deviation", value.deviation);
        return json;
    }

    public static GaussianValue readFromJson(JsonObject json) {
        return new GaussianValue(JsonUtils.getFloat(json, "mean"), JsonUtils.getFloat(json, "deviation"));
    }

    public static void writeToBuf(GaussianValue value, ByteBuf buf) {
        buf.writeFloat(value.mean);
        buf.writeFloat(value.deviation);
    }

    public static GaussianValue readFromBuf(ByteBuf buf) {
        return new GaussianValue(buf.readFloat(), buf.readFloat());
    }
}
