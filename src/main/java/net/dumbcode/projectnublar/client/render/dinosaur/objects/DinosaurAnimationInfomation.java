package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Value;
import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurAnimations;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Value
public class DinosaurAnimationInfomation {
    Map<DinosaurAnimations, List<PoseObject>> poses;
    int version;

    public enum Deserializer implements JsonDeserializer<DinosaurAnimationInfomation> {
        INSTANCE;

        @Override
        public DinosaurAnimationInfomation deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = element.getAsJsonObject();
            JsonObject poses = JsonUtils.getJsonObject(json, "poses");
            Map<DinosaurAnimations, List<PoseObject>> map = Maps.newHashMap();
            for (DinosaurAnimations animation : DinosaurAnimations.values()) {
                String animationName = animation.name();//.toLowerCase(Locale.ROOT);
                if(JsonUtils.hasField(poses, animationName)) {
                    for (JsonElement pose : JsonUtils.getJsonArray(poses, animationName)) {
                        map.computeIfAbsent(animation, a -> Lists.newArrayList()).add(context.deserialize(pose, PoseObject.class));
                    }
                }
            }
            return new DinosaurAnimationInfomation(
                    map,
                    JsonUtils.isNumber(json.get("version")) ? JsonUtils.getInt(json, "version") : 0
            );
        }
    }
}