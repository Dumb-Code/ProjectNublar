package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.entity.component.impl.AiComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.nbt.NBTTagCompound;

public class WanderComponent implements AiComponent {

    public boolean avoidWater = false;
    public int priority = 3;
    public double speed = 0.3D;
    public int chance = 120;


    @Override
    public void apply(EntityAITasks tasks, Entity entity) {
        if(entity instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) entity;
            tasks.addTask(this.priority, this.avoidWater ? new EntityAIWanderAvoidWater(creature, this.speed, 1F / this.chance) : new EntityAIWander(creature, this.speed, this.chance));
        } else {
            throw new IllegalArgumentException("Tried to attack a wander component to an entity of class " + entity.getClass() + ". The given entity must be a subclass of EntityCreature");
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {

    }
}
