package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Data;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Data
public class ModelProperties {
    private List<GrowthStage> modelGrowthStages = Lists.newArrayList(GrowthStage.ADULT);
    private Map<GrowthStage, String> mainModelMap = Maps.newEnumMap(GrowthStage.class);
    @SideOnly(Side.CLIENT)
    private ModelContainer.AnimatorFactory entityAnimatorSupplier;

    public ModelProperties() {
        if(FMLCommonHandler.instance().getSide().isClient()) {
            this.entityAnimatorSupplier = DinosaurAnimator::new; //Thinking lvl 400
        }
    }

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
                GrowthStage stage = GrowthStage.valueOf(stageName);
                result.modelGrowthStages.add(stage);
            }
            JsonObject modelMap = object.getAsJsonObject("model_map");
            for(Map.Entry<String, JsonElement> entry : modelMap.entrySet()) {
                String stageName = entry.getKey().toUpperCase();
                GrowthStage stage = GrowthStage.valueOf(stageName);
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

            for(Map.Entry<GrowthStage, String> entry : properties.mainModelMap.entrySet()) {
                modelMap.addProperty(entry.getKey().name().toLowerCase(), entry.getValue());
            }
            object.add("model_map", modelMap);
            return object;
        }
    }
}
