package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.projectnublar.server.dinosaur.data.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.system.ai.FeedingAi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;

public class MetabolismComponent implements AiComponent {
    public int food;
    public int water;

    public int foodRate;
    public int waterRate;

    public FeedingDiet diet;
    public int foodSmellDistance;
    @Override
    public NBTTagCompound serialize(NBTTagCompound compound)
    {
        compound.setInteger("food", this.food);
        compound.setInteger("water", this.water);

        compound.setTag("diet", this.diet.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound)
    {
        this.food = compound.getInteger("food");
        this.water = compound.getInteger("water");

        this.diet.fromNBT(compound.getCompoundTag("diet"));
    }

    @Override
    public void apply(EntityAITasks tasks, Entity entity) {
        if(entity instanceof EntityLiving) {
            tasks.addTask(2, new FeedingAi((EntityLiving) entity, this));
        }
    }
}
