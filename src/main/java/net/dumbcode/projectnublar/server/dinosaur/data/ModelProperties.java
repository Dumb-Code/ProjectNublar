package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Data;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Data
public class ModelProperties {
    private List<ModelStage> modelGrowthStages = Lists.newArrayList(ModelStage.ADULT, ModelStage.SKELETON);
    private Map<ModelStage, String> mainModelMap = Maps.newEnumMap(ModelStage.class);
    @SideOnly(Side.CLIENT)
    private float entityAnimatorSupplier;


    public void copyFrom(ModelProperties other) {
        modelGrowthStages.clear();
        modelGrowthStages.addAll(other.modelGrowthStages);
        mainModelMap.clear();
        mainModelMap.putAll(other.mainModelMap);
    }

    public static class JsonAdapter implements JsonSerializer<ModelProperties>, JsonDeserializer<ModelProperties> {

        @Override
        public ModelProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            ModelProperties result = new ModelProperties();
            result.modelGrowthStages.clear();
            JsonArray growthStages = object.getAsJsonArray("growth_stages");
            for(JsonElement elem : growthStages) {
                String stageName = elem.getAsString().toUpperCase();
                ModelStage stage = ModelStage.valueOf(stageName);
                result.modelGrowthStages.add(stage);
            }
            JsonObject modelMap = object.getAsJsonObject("model_map");
            for(Map.Entry<String, JsonElement> entry : modelMap.entrySet()) {
                String stageName = entry.getKey().toUpperCase();
                ModelStage stage = ModelStage.valueOf(stageName);
                String model = entry.getValue().getAsString();
                result.mainModelMap.put(stage, model);
            }
            return result;
        }

        @Override
        public JsonElement serialize(ModelProperties properties, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            JsonArray growStages = new JsonArray();
            properties.modelGrowthStages.forEach(stage -> growStages.add(stage.name().toLowerCase()));
            object.add("growth_stages", growStages);

            JsonObject modelMap = new JsonObject();

            for(Map.Entry<ModelStage, String> entry : properties.mainModelMap.entrySet()) {
                modelMap.addProperty(entry.getKey().name().toLowerCase(), entry.getValue());
            }
            object.add("model_map", modelMap);
            return object;
        }
    }
}
