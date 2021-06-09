package net.dumbcode.projectnublar.server.entity.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.SaveableEntityStorage;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DinosaurMultipartStorage implements SaveableEntityStorage<MultipartEntityComponent> {

    private final Map<String, List<String>> ageCubeMap = Maps.newHashMap();

    private final Function<ComponentAccess, List<String>> function = access -> access.get(ComponentHandler.AGE).flatMap(AgeComponent::getModelState).map(AgeStage::getName).map(this.ageCubeMap::get).orElse(Lists.newArrayList());

    public DinosaurMultipartStorage addCubesForAge(String age, String... cubes) {
        this.ageCubeMap.put(age, Lists.newArrayList(cubes));
        return this;
    }

    @Override
    public void constructTo(MultipartEntityComponent component) {
        component.setMultipartNames(this.function);
    }

    @Override
    public void readJson(JsonObject json) {
        this.ageCubeMap.clear();
        JsonArray cubeMap = JSONUtils.getAsJsonArray(json, "cube_map");
        for (JsonElement element : cubeMap) {
            JsonObject cubeMapEntry = element.getAsJsonObject();

            this.ageCubeMap.put(JSONUtils.getAsString(cubeMapEntry, "age"),
                    StreamSupport.stream(JSONUtils.getAsJsonArray(cubeMapEntry, "cubes").spliterator(), false)
                            .filter(JsonElement::isJsonPrimitive)
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList())
            );
        }
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        this.ageCubeMap.clear();
        for (INBT element : nbt.getList("cube_map", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT cubeMapEntry = (CompoundNBT) element;

            this.ageCubeMap.put(cubeMapEntry.getString("age"),
                    cubeMapEntry.getList("cubes", Constants.NBT.TAG_STRING).stream()
                            .map(b -> ((StringNBT)b).getAsString())
                            .collect(Collectors.toList())
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
        json.add("cube_map", jarr);
    }

    @Override
    public CompoundNBT writeNBT(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Map.Entry<String, List<String>> entry : this.ageCubeMap.entrySet()) {
            CompoundNBT cubeMapEntry = new CompoundNBT();
            cubeMapEntry.putString("age", entry.getKey());
            ListNBT cubes = new ListNBT();
            for (String s : entry.getValue()) {
                cubes.add(StringNBT.valueOf(s));
            }
            cubeMapEntry.put("cubes", cubes);
            list.add(cubeMapEntry);
        }
        nbt.put("cube_map", list);
        return nbt;
    }
}
