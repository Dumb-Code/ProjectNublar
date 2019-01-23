package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Data;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

@Data
public class EntityProperties {
    private Function<World, DinosaurEntity> entityCreateFunction = DinosaurEntity::new;

    private Map<ModelStage, Integer> tickStageMap = Maps.newEnumMap(ModelStage.class);

    public EntityProperties() {
        for (ModelStage value : ModelStage.values()) {
            this.tickStageMap.put(value, value.getDefaultTickAge());
        }
    }

    public void copyFrom(EntityProperties other) {
        this.tickStageMap.clear();
        this.tickStageMap.putAll(other.tickStageMap);
    }

    public static class JsonAdapter implements JsonSerializer<EntityProperties>, JsonDeserializer<EntityProperties> {

        @Override
        public EntityProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityProperties result = new EntityProperties();

            JsonObject obj = json.getAsJsonObject();
            JsonElement alive = obj.get("alive_map");
            if(alive instanceof JsonObject) {
                JsonObject aliveobj = alive.getAsJsonObject();
                for (ModelStage stage : ModelStage.values()) {
                    if(JsonUtils.isJsonPrimitive(aliveobj, stage.getName())) {
                        result.tickStageMap.put(stage, aliveobj.getAsJsonPrimitive(stage.getName()).getAsInt());
                    }
                }
            }

            return result;
        }

        @Override
        public JsonElement serialize(EntityProperties src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();

            JsonObject alive = new JsonObject();
            for (Map.Entry<ModelStage, Integer> entry : src.tickStageMap.entrySet()) {
                if(entry.getKey().getDefaultTickAge() != entry.getValue()) {
                    alive.addProperty(entry.getKey().getName(), entry.getValue());
                }
            }
            object.add("alive_map", alive);

            return object;
        }
    }
}
