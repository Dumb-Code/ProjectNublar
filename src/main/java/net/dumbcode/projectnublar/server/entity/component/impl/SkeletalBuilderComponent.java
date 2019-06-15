package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;

public class SkeletalBuilderComponent implements EntityComponent {

    @Getter private List<String> individualBones = Lists.newArrayList();
    @Getter private List<String> boneListed = Lists.newArrayList();
    @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

    public int modelIndex;
    public ModelStage stage = ModelStage.ADULT;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("model_index", this.modelIndex);
        compound.setInteger("stage", this.stage.ordinal());

        NBTTagCompound tag = new NBTTagCompound();
        for(Map.Entry<String, List<String>> entry : this.boneToModelMap.entrySet()) {
            String bone = entry.getKey();
            List<String> modelParts = entry.getValue();
            if(modelParts.size() == 1) {
                tag.setString(bone, modelParts.get(0));
            } else {
                NBTTagList list = new NBTTagList();
                modelParts.forEach(s -> list.appendTag(new NBTTagString(s)));
                tag.setTag(bone, list);
            }
        }
        compound.setTag("bone_map", tag);

        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.modelIndex = compound.getInteger("model_index");
        this.stage = ModelStage.values()[compound.getInteger("stage")];

        NBTTagCompound tag = compound.getCompoundTag("bone_map");
        Map<String, List<String>> nbtBoneMap = new HashMap<>();
        for (String bone : tag.getKeySet()) {
            NBTBase base = tag.getTag(bone);
            if(base instanceof NBTTagList) {
                NBTTagList array = (NBTTagList) base;
                List<String> list = new LinkedList<>();
                array.forEach(elem -> list.add(((NBTTagString)elem).getString()));
                nbtBoneMap.put(bone, list);
            } else {
                nbtBoneMap.put(bone, Collections.singletonList(((NBTTagString)base).getString()));
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

    public static class Storage implements EntityComponentStorage<SkeletalBuilderComponent> {

        @Getter private List<String> individualBones = Lists.newArrayList();
        @Getter private List<String> boneListed = Lists.newArrayList();

        @Getter private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

        @Getter @Setter private ModelStage stage = ModelStage.ADULT;

        @Override
        public SkeletalBuilderComponent construct() {
            SkeletalBuilderComponent component = new SkeletalBuilderComponent();

            component.individualBones = this.individualBones;
            component.boneListed = this.boneListed;
            component.boneToModelMap = this.boneToModelMap;

            component.stage = this.stage;

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

        @Override
        public void readJson(JsonObject json) {
            this.stage = ModelStage.valueOf(json.get("stage").getAsString().toUpperCase());
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
            json.addProperty("stage", this.stage.name());
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
