package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class HerdComponent implements AiComponent {

    public UUID herdUUID;
    public boolean leader;
    public UUID entityUUID;
    @Nullable public HerdComponent herd;
    public List<UUID> members = Lists.newArrayList();
    public List<UUID> enemies = Lists.newArrayList();

    public ResourceLocation herdTypeID;

    public int tryMoveCooldown;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        if(this.herdUUID != null) {
            compound.setUniqueId("uuid", this.herdUUID);
        }
        compound.setBoolean("leader", this.leader);

        saveList(compound.getCompoundTag("members"), this.members);
        saveList(compound.getCompoundTag("enemies"), this.enemies);

        compound.setString("herd_type_id", this.herdTypeID.toString());

        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        if(compound.hasUniqueId("uuid")) {
            this.herdUUID = compound.getUniqueId("uuid");
        } else {
            this.herdUUID = null;
        }
        this.leader = compound.getBoolean("leader");

        loadList(compound.getCompoundTag("members"), this.members);
        loadList(compound.getCompoundTag("enemies"), this.enemies);

        this.herdTypeID = new ResourceLocation(compound.getString("herd_type_id"));
    }

    public List<Entity> getMembers(World world) {
        return world.loadedEntityList.stream().filter(e -> this.members.contains(e.getUniqueID())).collect(Collectors.toList());
    }

    public List<HerdComponent> getComponents(World world) {
        return this.getMembers(world).stream()
                .filter(ComponentAccess.class::isInstance)
                .map(e -> ((ComponentAccess)e).getOrNull(EntityComponentTypes.HERD))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Entity> getEnemies(World world) {
        return world.loadedEntityList.stream().filter(e -> this.enemies.contains(e.getUniqueID())).collect(Collectors.toList());
    }

    private void saveList(NBTTagCompound nbt, List<UUID> list) {
        nbt.setInteger("size", list.size());
        for (int i = 0; i < list.size(); i++) {
            nbt.setUniqueId(String.valueOf(i), list.get(i));
        }
    }

    private static void loadList(NBTTagCompound nbt, List<UUID> list) {
        list.clear();
        for (int i = 0; i < nbt.getInteger("size"); i++) {
            list.add(nbt.getUniqueId(String.valueOf(i)));
        }
    }

    public void addMember(UUID uniqueID, HerdComponent herd) {
        if(herd.herd != null) {
            herd.herd.members.remove(uniqueID);
        }
        this.members.add(uniqueID);
        herd.herd = this;
        herd.herdUUID = this.herdUUID;
    }

    @Override
    public void apply(EntityAITasks tasks, Entity entity) {
        this.entityUUID = entity.getUniqueID();
        if(entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(120.0D);
        }

    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<HerdComponent> {

        private ResourceLocation herdTypeID; //Used to check if entities are compatible with other herds ect.

        @Override
        public HerdComponent construct() {
            HerdComponent component = new HerdComponent();
            component.herdTypeID = this.herdTypeID;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.herdTypeID = new ResourceLocation(json.get("herd_type_id").getAsString());
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("herd_type_id", this.herdTypeID.toString());
        }
    }
}
