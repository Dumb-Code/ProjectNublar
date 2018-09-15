package net.dumbcode.projectnublar.server.entity.component.impl;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

public class GenderComponent implements EntityComponent {
    public boolean male = false;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setBoolean("male", this.male);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.male = compound.getBoolean("male");
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.male);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.male = buf.readBoolean();
    }
}
