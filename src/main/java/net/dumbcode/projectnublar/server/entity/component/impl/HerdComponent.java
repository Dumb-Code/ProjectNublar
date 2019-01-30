package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.UUID;

public class HerdComponent implements EntityComponent {

    public UUID herdUUID = new UUID(0,0);
    public boolean leader;
    public boolean inHerd;
    public List<UUID> members = Lists.newArrayList();
    public List<UUID> enemies = Lists.newArrayList();


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
}
