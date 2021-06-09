package net.dumbcode.projectnublar.server.entity.storage;

import com.google.gson.JsonObject;
import net.dumbcode.dumblibrary.server.ecs.component.SaveableEntityStorage;
import net.dumbcode.dumblibrary.server.ecs.component.impl.CloseProximityAngryComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CloseProximityBlacklistStorage extends CloseProximityAngryComponent.Storage implements SaveableEntityStorage<CloseProximityAngryComponent> {

    private List<EntityType<?>> blackListClasses = new ArrayList<>();

    @Override
    public void constructTo(CloseProximityAngryComponent component) {
        super.constructTo(component);
        component.setPredicate(e -> !blackListClasses.contains(e.getType()));
    }

    public CloseProximityBlacklistStorage add(EntityType<?>... classes) {
        Collections.addAll(this.blackListClasses, classes);
        return this;
    }

    @Override
    public CloseProximityBlacklistStorage setRange(float range) {
        super.setRange(range);
        return this;
    }

    @Override
    public void writeJson(JsonObject json) {
        super.writeJson(json);
        json.add("blacklist_classes", this.blackListClasses.stream()
            .map(t -> t.getRegistryName().toString())
            .collect(CollectorUtils.toJsonArrayString())
        );
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.blackListClasses = StreamUtils.stream(JSONUtils.getAsJsonArray(json, "blacklist_classes"))
            .map(j -> ForgeRegistries.ENTITIES.getValue(new ResourceLocation(j.getAsString())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        this.blackListClasses = nbt.getList("blacklist_classes", Constants.NBT.TAG_STRING).stream()
            .map(j -> ForgeRegistries.ENTITIES.getValue(new ResourceLocation(j.getAsString())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public CompoundNBT writeNBT(CompoundNBT nbt) {
        nbt.put("blacklist_classes", this.blackListClasses.stream()
            .map(t -> StringNBT.valueOf(t.getRegistryName().toString()))
            .collect(CollectorUtils.toNBTTagList())
        );
        return nbt;
    }
}
