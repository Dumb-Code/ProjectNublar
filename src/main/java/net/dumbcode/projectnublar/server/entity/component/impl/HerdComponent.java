package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HerdComponent implements EntityComponent {

    public UUID herdUUID = new UUID(0,0);
    public boolean leader;
    public boolean inHerd;
    @Nullable public HerdComponent herd;
    public List<UUID> members = Lists.newArrayList();
    public List<UUID> enemies = Lists.newArrayList();

    public Predicate<Entity> acceptedEntitiy;


    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setUniqueId("uuid", this.herdUUID);
        compound.setBoolean("leader", this.leader);
        compound.setBoolean("inHerd", this.inHerd);

        saveList(compound.getCompoundTag("members"), this.members);
        saveList(compound.getCompoundTag("enemies"), this.enemies);

        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.herdUUID = compound.getUniqueId("uuid");
        this.leader = compound.getBoolean("leader");
        this.inHerd = compound.getBoolean("inHerd");

        loadList(compound.getCompoundTag("members"), this.members);
        loadList(compound.getCompoundTag("enemies"), this.enemies);
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
        herd.inHerd = true;
    }
}
