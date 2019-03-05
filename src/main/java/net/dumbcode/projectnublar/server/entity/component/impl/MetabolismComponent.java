package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

public class MetabolismComponent implements EntityComponent
{

    public int food;
    public int water;

    public int foodRate;
    public int waterRate;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound)
    {
        compound.setInteger("food", this.food);
        compound.setInteger("water", this.water);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound)
    {
        this.food = compound.getInteger("food");
        this.water = compound.getInteger("water");
    }

}
