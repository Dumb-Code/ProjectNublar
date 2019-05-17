package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.*;

@Data
//Todo: move whole class to a Storage on the Skeletal Builder
public class SkeletalInformation {
    private List<String> individualBones = Lists.newArrayList();
    private List<String> boneListed = Lists.newArrayList();
    private Map<String, List<String>> boneToModelMap = Maps.newHashMap();

    //Currently, if a cube dosnt have a parent (or the parents parents ect...) linked with a bone, it dosnt render at all. TODO: fix that
    public void initializeMap(String... boneModels) {
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
    }

    public void initializeFromBoneMap(Map<String, List<String>> boneMap) {
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

    public void copyFrom(SkeletalInformation other) {
        individualBones.clear();
        individualBones.addAll(other.individualBones);
        boneToModelMap.clear();
        // deep copy
        for(Map.Entry<String, List<String>> entry : other.boneToModelMap.entrySet()) {
            List<String> copy = new LinkedList<>(entry.getValue());
            boneToModelMap.put(entry.getKey(), copy);
        }
    }

    public static class JsonAdapter implements JsonSerializer<SkeletalInformation>, JsonDeserializer<SkeletalInformation> {

        @Override
        public SkeletalInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            SkeletalInformation information = new SkeletalInformation();
            JsonObject boneMap = object.getAsJsonObject("bone_map");
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
            information.initializeFromBoneMap(boneToModelMap);
            return information;
        }

        @Override
        public JsonElement serialize(SkeletalInformation info, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            JsonObject boneMap = new JsonObject();
            Map<String, List<String>> map = info.boneToModelMap;
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
            object.add("bone_map", boneMap);
            return object;
        }
    }
}
