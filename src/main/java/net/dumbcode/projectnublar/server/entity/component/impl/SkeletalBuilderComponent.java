package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.*;

//TODO:
//  - change this to allow for an ast tree for the bones, rather than a list of them. For example
//    when we get to the ribcage, they player should be allowed to either go to the tail, or the neck, rather than always the tail first
//  - Make this instead of caching the model to look into the model component and get that
public class SkeletalBuilderComponent extends EntityComponent implements RenderLocationComponent {

    @Getter private List<String> individualBones = Lists.newArrayList();
    @Getter private List<String> boneListed = Lists.newArrayList();
    @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

    public int modelIndex;

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.putInt("model_index", this.modelIndex);

        CompoundTag tag = new CompoundTag();
        for(Map.Entry<String, List<String>> entry : this.boneToModelMap.entrySet()) {
            String bone = entry.getKey();
            List<String> modelParts = entry.getValue();
            if(modelParts.size() == 1) {
                tag.putString(bone, modelParts.get(0));
            } else {
                ListNBT list = new ListNBT();
                modelParts.forEach(s -> list.add(StringNBT.valueOf(s)));
                tag.put(bone, list);
            }
        }
        compound.put("bone_map", tag);

        return compound;
    }

    @Override
    public void deserialize(CompoundTag compound) {
        this.modelIndex = compound.getInt("model_index");

        CompoundTag tag = compound.getCompound("bone_map");
        Map<String, List<String>> nbtBoneMap = new HashMap<>();
        for (String bone : tag.getAllKeys()) {
            INBT base = tag.get(bone);
            if(base instanceof ListNBT) {
                ListNBT array = (ListNBT) base;
                List<String> list = new LinkedList<>();
                array.forEach(elem -> list.add(elem.toString()));
                nbtBoneMap.put(bone, list);
            } else if(base instanceof StringNBT) {
                nbtBoneMap.put(bone, Collections.singletonList(base.toString()));
            }
        }
        initializeFromBoneMap(this.individualBones, this.boneListed, this.boneToModelMap, nbtBoneMap);
    }

    public static void initializeFromBoneMap(List<String> individualBones, List<String> boneListed, Map<String, List<String>> boneToModelMap, Map<String, List<String>> boneMap) {
        individualBones.clear();
        boneListed.clear();
        for(Map.Entry<String, List<String>> entry : boneMap.entrySet()) {
            String bone = entry.getKey();
            if(!individualBones.contains(bone)) {
                individualBones.add(bone);
            }
            boneListed.add(bone);
            boneToModelMap.put(bone, entry.getValue());
        }
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        texture.addFileName("skeleton", 100);

        fileLocation.addName("skeleton", 100);
    }

    public static class Storage implements EntityComponentStorage<SkeletalBuilderComponent> {

        @Getter private List<String> individualBones = Lists.newArrayList();
        @Getter private List<String> boneListed = Lists.newArrayList();

        @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

        @Override
        public void constructTo(SkeletalBuilderComponent component) {

            component.individualBones = this.individualBones;
            component.boneListed = this.boneListed;
            component.boneToModelMap = this.boneToModelMap;


        }

        //Currently, if a cube dosnt have a parent (or the parents parents ect...) linked with a bone, it dosnt render at all. TODO: fix that
        public Storage initializeMap(String... boneModels) {
            if(boneModels.length % 2 != 0) {
                throw new RuntimeException("Dont know how to handle list of length " + boneModels.length);
            }
            for (int i = 0; i < boneModels.length; i+=2) {
                String bone = boneModels[i];
                if(!this.individualBones.contains(bone)) {
                    this.individualBones.add(bone);
                }
                this.boneListed.add(bone);
                this.boneToModelMap.computeIfAbsent(bone, s -> Lists.newArrayList()).add(boneModels[i + 1]);
            }
            return this;
        }

        @Override
        public void readJson(JsonObject json) {
            JsonObject boneMap = json.getAsJsonObject("bone_map");
            Map<String, List<String>> jsonBoneMap = new HashMap<>();
            for(Map.Entry<String, JsonElement> entry : boneMap.entrySet()) {
                String bone = entry.getKey();
                JsonElement element = entry.getValue();
                if(element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    List<String> list = new LinkedList<>();
                    array.forEach(elem -> list.add(elem.getAsString()));
                    jsonBoneMap.put(bone, list);
                } else {
                    jsonBoneMap.put(bone, Collections.singletonList(element.getAsString()));
                }
            }
            initializeFromBoneMap(this.individualBones, this.boneListed, this.boneToModelMap, jsonBoneMap);
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonObject boneMap = new JsonObject();
            for(Map.Entry<String, List<String>> entry : this.boneToModelMap.entrySet()) {
                String bone = entry.getKey();
                List<String> modelParts = entry.getValue();
                if(modelParts.size() == 1) {
                    boneMap.addProperty(bone, modelParts.get(0));
                } else {
                    JsonArray parts = new JsonArray();
                    modelParts.forEach(parts::add);
                    boneMap.add(bone, parts);
                }
            }
            json.add("bone_map", boneMap);
        }
    }
}
