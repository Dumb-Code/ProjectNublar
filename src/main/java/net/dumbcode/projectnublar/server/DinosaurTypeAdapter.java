package net.dumbcode.projectnublar.server;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DinosaurTypeAdapter extends TypeAdapter {
    @Override
    public void write(JsonWriter out, Object value) throws IOException {
        Dinosaur dino = (Dinosaur)value;
        JsonWriter writer = out.beginObject();
        writer
                .name("id")
                .value(dino.getRegName().toString());
        writer.name("growth_stages").beginObject();
        for(GrowthStage stage : GrowthStage.values()) {
            String modelName = dino.getModelProperties().getMainModelMap().get(stage);
            if(modelName != null) {
                writer.name(stage.name().toLowerCase()).value(modelName);
            }
        }
        writer.endObject();

        writer
            .name("item_attributes")
            .beginObject()
                .name("cooked_meat")
                .beginObject()
                    .name("heal_amount")
                    .value(dino.getItemProperties().getCookedMeatHealAmount())
                    .name("saturation")
                    .value(dino.getItemProperties().getCookedMeatSaturation())
                .endObject()
                .name("raw_meat")
                .beginObject()
                    .name("heal_amount")
                    .value(dino.getItemProperties().getRawMeatHealAmount())
                    .name("saturation")
                    .value(dino.getItemProperties().getRawMeatSaturation())
                .endObject()
                .name("cooking_experience")
                .value(dino.getItemProperties().getCookingExperience())
            .endObject();
        writer
                .name("skeletal_information")
                .beginObject()
                    .name("bone_map")
                    .beginObject();

        Map<String, List<String>> boneMap = dino.getSkeletalInformation().getBoneToModelMap();
        for(Map.Entry<String, List<String>> entry : boneMap.entrySet()) {
            writer.name(entry.getKey());
            List<String> list = entry.getValue();
            if(list.size() == 1) {
                writer.value(list.get(0));
            } else {
                writer.beginArray();
                for (String part : list) {
                    writer.value(part);
                }
                writer.endArray();
            }
        }
        writer.endObject().endObject();

        out.endObject();
    }

    @Override
    public Dinosaur read(JsonReader in) {
        return null;
    }
}
