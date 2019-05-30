package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class SkeletalBuilderCompoent implements EntityComponent {

    @Getter private List<String> individualBones = Lists.newArrayList();
    @Getter private List<String> boneListed = Lists.newArrayList();
    @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

    public int modelIndex;
    public ModelStage stage = ModelStage.ADULT;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("model_index", this.modelIndex);
        compound.setInteger("stage", this.stage.ordinal());
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.modelIndex = compound.getInteger("model_index");
        this.stage = ModelStage.values()[compound.getInteger("stage")];
    }

    public static class Storage implements EntityComponentStorage<SkeletalBuilderCompoent> {

        @Getter private List<String> individualBones = Lists.newArrayList();
        @Getter private List<String> boneListed = Lists.newArrayList();
        @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

        @Override
        public SkeletalBuilderCompoent construct() {
            SkeletalBuilderCompoent component = new SkeletalBuilderCompoent();

            component.individualBones = this.individualBones;
            component.boneListed = this.boneListed;
            component.boneToModelMap = this.boneToModelMap;

            return component;
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

        public Storage initializeFromBoneMap(Map<String, List<String>> boneMap) {
            this.individualBones.clear();
            this.boneListed.clear();
            for(Map.Entry<String, List<String>> entry : boneMap.entrySet()) {
                String bone = entry.getKey();
                if(!this.individualBones.contains(bone)) {
                    this.individualBones.add(bone);
                }
                this.boneListed.add(bone);
                this.boneToModelMap.put(bone, entry.getValue());
            }
            return this;
        }

        @Override
        public void readJson(JsonObject json) {
            JsonObject boneMap = json.getAsJsonObject("bone_map");
            Map<String, List<String>> boneToModelMap = new HashMap<>();
            for(Map.Entry<String, JsonElement> entry : boneMap.entrySet()) {
                String bone = entry.getKey();
                JsonElement element = entry.getValue();
                if(element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    List<String> list = new LinkedList<>();
                    array.forEach(elem -> list.add(elem.getAsString()));
                    boneToModelMap.put(bone, list);
                } else {
                    boneToModelMap.put(bone, Collections.singletonList(element.getAsString()));
                }
            }
            this.initializeFromBoneMap(boneToModelMap);
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonObject boneMap = new JsonObject();
            Map<String, List<String>> map = this.boneToModelMap;
            for(Map.Entry<String, List<String>> entry : map.entrySet()) {
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
