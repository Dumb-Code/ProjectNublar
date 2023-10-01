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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CloseProximityBlacklistStorage extends CloseProximityAngryComponent.Storage implements SaveableEntityStorage<CloseProximityAngryComponent> {

    private List<ResourceLocation> blackListClasses = new ArrayList<>();

    @Override
    public void constructTo(CloseProximityAngryComponent component) {
        super.constructTo(component);
        component.setPredicate(e -> !blackListClasses.contains(e.getType().getRegistryName()));
    }

    public CloseProximityBlacklistStorage add(ResourceLocation... classes) {
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
            .map(ResourceLocation::toString)
            .collect(CollectorUtils.toJsonArrayString())
        );
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.blackListClasses = StreamUtils.stream(JSONUtils.getAsJsonArray(json, "blacklist_classes"))
            .map(j -> new ResourceLocation(j.getAsString()))
            .collect(Collectors.toList());
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        this.blackListClasses = nbt.getList("blacklist_classes", Constants.NBT.TAG_STRING).stream()
            .map(j -> new ResourceLocation(j.getAsString()))
            .collect(Collectors.toList());
    }

    @Override
    public CompoundNBT writeNBT(CompoundNBT nbt) {
        nbt.put("blacklist_classes", this.blackListClasses.stream()
            .map(t -> StringNBT.valueOf(t.toString()))
            .collect(CollectorUtils.toNBTTagList())
        );
        return nbt;
    }
}
