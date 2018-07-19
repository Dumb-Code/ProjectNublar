package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import com.google.gson.*;
import lombok.Data;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

@Data
public class PoseObject {
    private final String poseLocation;
    private final float ticksTime;

    public enum Deserializer implements JsonDeserializer<PoseObject> {
        INSTANCE;

        @Override
        public PoseObject deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();
            return new PoseObject(
                    JsonUtils.getString(json ,"pose"),
                    JsonUtils.getFloat(json, "time")
            );
        }
    }
}
