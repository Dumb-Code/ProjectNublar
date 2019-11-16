package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.nbt.NBTTagCompound;

public class WanderComponent extends EntityComponent implements FinalizableComponent {

    private boolean avoidWater = false;
    private int priority = 3;
    private double speed = 0.5D;
    private int chance = 50;


    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) entity;
            ((EntityCreature) entity).tasks.addTask(this.priority, this.avoidWater ? new EntityAIWanderAvoidWater(creature, this.speed, 1F / this.chance) : new EntityAIWander(creature, this.speed, this.chance));
        } else {
            throw new IllegalArgumentException("Tried to attach a wander component to an ecs of class " + entity.getClass() + ". The given ecs must be a subclass of EntityCreature");
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
