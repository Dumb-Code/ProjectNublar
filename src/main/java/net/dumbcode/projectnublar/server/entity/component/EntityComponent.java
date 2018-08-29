package net.dumbcode.projectnublar.server.entity.component;

import net.minecraft.nbt.NBTTagCompound;

public interface EntityComponent {
    NBTTagCompound serialize(NBTTagCompound compound);

    void deserialize(NBTTagCompound compound);
}
