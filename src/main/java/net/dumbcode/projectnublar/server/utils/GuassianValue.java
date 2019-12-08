package net.dumbcode.projectnublar.server.utils;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.Random;

@AllArgsConstructor
public class GuassianValue {
    private final float mean;
    private final float deviation;

    public float getRandomValue(Random random) {
        return (float) (this.mean + random.nextGaussian() * this.deviation);
    }

    public static NBTTagCompound writeToNBT(GuassianValue value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("mean", value.mean);
        nbt.setFloat("deviation", value.deviation);
        return nbt;
    }

    public static GuassianValue readFromNBT(NBTTagCompound nbt) {
        return new GuassianValue(nbt.getFloat("mean"), nbt.getFloat("deviation"));
    }

    public static JsonObject writeToJson(GuassianValue value) {
        JsonObject json = new JsonObject();
        json.addProperty("mean", value.mean);
        json.addProperty("deviation", value.deviation);
        return json;
    }

    public static GuassianValue readFromJson(JsonObject json) {
        return new GuassianValue(JsonUtils.getFloat(json, "mean"), JsonUtils.getFloat(json, "deviation"));
    }

    public static void writeToBuf(GuassianValue value, ByteBuf buf) {
        buf.writeFloat(value.mean);
        buf.writeFloat(value.deviation);
    }

    public static GuassianValue readFromBuf(ByteBuf buf) {
        return new GuassianValue(buf.readFloat(), buf.readFloat());
    }
}
