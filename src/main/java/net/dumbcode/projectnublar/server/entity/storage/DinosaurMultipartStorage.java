package net.dumbcode.projectnublar.server.entity.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.SaveableEntityStorage;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.JsonUtils;
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
    public MultipartEntityComponent constructTo(MultipartEntityComponent component) {
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
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList())
            );
        }
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.ageCubeMap.clear();
        for (NBTBase element : nbt.getTagList("cube_map", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound cubeMapEntry = (NBTTagCompound) element;

            this.ageCubeMap.put(cubeMapEntry.getString("age"),
                    StreamSupport.stream(cubeMapEntry.getTagList("cubes", Constants.NBT.TAG_STRING).spliterator(), false)
                            .map(b -> ((NBTTagString)b).getString())
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
    public NBTTagCompound writeNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, List<String>> entry : this.ageCubeMap.entrySet()) {
            NBTTagCompound cubeMapEntry = new NBTTagCompound();
            cubeMapEntry.setString("age", entry.getKey());
            NBTTagList cubes = new NBTTagList();
            for (String s : entry.getValue()) {
                cubes.appendTag(new NBTTagString(s));
            }
            cubeMapEntry.setTag("cubes", cubes);
            list.appendTag(cubeMapEntry);
        }
        nbt.setTag("cube_map", list);
        return nbt;
    }
}
