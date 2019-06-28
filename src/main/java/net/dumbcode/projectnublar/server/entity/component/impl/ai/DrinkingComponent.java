package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import net.dumbcode.dumblibrary.server.entity.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.entity.ai.DrinkingAI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;

public class DrinkingComponent implements FinalizableComponent {

    public int priority = 4;
    private float speed = 11f;

    @Override
    public void finalizeComponent(Entity entity) {
        if(entity instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) entity;
            ((EntityCreature) entity).tasks.addTask(this.priority, new DrinkingAI(creature, speed));
        } else {
            throw new IllegalArgumentException("Tried to attach a drinking component to an entity of class " + entity.getClass() + ". The given entity must be a subclass of EntityCreature");
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
