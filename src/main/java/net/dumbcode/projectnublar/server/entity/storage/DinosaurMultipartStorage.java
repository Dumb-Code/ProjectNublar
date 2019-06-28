package net.dumbcode.projectnublar.server.entity.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.entity.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.util.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
public class DinosaurMultipartStorage implements EntityComponentStorage<MultipartEntityComponent> {

    private final Map<String, List<String>> ageCubeMap = Maps.newHashMap();

    private final Function<ComponentAccess, List<String>> function = access -> access.get(ComponentHandler.AGE).map(AgeComponent::getStage).map(AgeStage::getName).map(this.ageCubeMap::get).orElse(Lists.newArrayList());

    @Override
    public MultipartEntityComponent construct() {
        MultipartEntityComponent component = new MultipartEntityComponent();
        component.setMultipartNames(this.function);
        return component;
    }


    @Override
    public void readJson(JsonObject json) {
        this.ageCubeMap.clear();
        JsonArray cubeMap = JsonUtils.getJsonArray(json, "cube_map");
        for (JsonElement element : cubeMap) {
            JsonObject cubeMapEntry = JsonUtils.getJsonObject(element, "cube_map_entry");

            this.ageCubeMap.put(JsonUtils.getString(cubeMapEntry, "age"),
            StreamSupport.stream(JsonUtils.getJsonArray(cubeMapEntry, "cubes").spliterator(), false)
                    .filter(JsonElement::isJsonPrimitive)
                    .map(JsonElement::getAsString
                    ).collect(Collectors.toList())
            );
        }
    }

    @Override
    public void writeJson(JsonObject json) {
        JsonArray jarr = new JsonArray();

        for (Map.Entry<String, List<String>> entry : this.ageCubeMap.entrySet()) {
            JsonObject cubeMapEntry = new JsonObject();

            cubeMapEntry.addProperty("age", entry.getKey());

            JsonArray cubes = new JsonArray();
            for (String s : entry.getValue()) {
                cubes.add(s);
            }

            cubeMapEntry.add("cubes", cubes);

            jarr.add(cubeMapEntry);
        }

    }
}
