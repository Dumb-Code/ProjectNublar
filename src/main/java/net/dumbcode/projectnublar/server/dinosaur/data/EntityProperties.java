package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.gson.*;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Data
@Accessors(chain = true)
public class EntityProperties {

    private Function<World, DinosaurEntity> entityCreateFunction = DinosaurEntity::new;
    private Map<ModelStage, Integer> tickStageMap = Maps.newEnumMap(ModelStage.class);
    private Map<ModelStage, List<String>> linkedCubeMap = Maps.newEnumMap(ModelStage.class);

    // Max Food and water that the entity can have.
    private int maxFood;
    private int maxWater;
    // Rate that the food and water decrease every second
    private int waterRate;
    private int foodRate;

    private float width; //todo serilize
    private float height;

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
            JsonElement entityMap = obj.get("entity_map");
            if(entityMap instanceof JsonObject) {
                JsonObject entityobj = entityMap.getAsJsonObject();
                for (ModelStage stage : ModelStage.values()) {
                    if(JsonUtils.isJsonArray(entityobj, stage.getName())) {
                        result.linkedCubeMap.computeIfAbsent(stage, m -> Lists.newArrayList())
                                .addAll(StreamSupport.stream(entityobj.getAsJsonArray(stage.getName()).spliterator(), false)
                                        .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).collect(Collectors.toList()));
                    }
                }
            }
            result.maxFood = obj.get("max_food").getAsInt();
            result.maxWater = obj.get("max_water").getAsInt();
            result.waterRate = obj.get("water_rate").getAsInt();
            result.foodRate = obj.get("food_rate").getAsInt();

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

            JsonObject entity = new JsonObject();
            for (Map.Entry<ModelStage, List<String>> entry : src.linkedCubeMap.entrySet()) {
                List<String> value = entry.getValue();
                if(value != null && !value.isEmpty()) {
                    JsonArray obj = new JsonArray();
                    for (String s : entry.getValue()) {
                        obj.add(s);
                    }
                    entity.add(entry.getKey().getName(), obj);
                }
            }
            object.add("entity_map", alive);

            return object;
        }
    }
}
