package net.dumbcode.projectnublar.server.entity.component.impl;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.MetabolismComponent;
import net.minecraft.nbt.NBTTagCompound;

@Getter
@Setter
public class MoodComponent extends EntityComponent implements FinalizableComponent {

    private MoodType mood = MoodType.HAPPY;
    private MetabolismComponent metabolism;

    private int foodThreshold = 50;
    private int waterThreshold = 50;
    private int currentMood = 100;

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if (entity.get(EntityComponentTypes.METABOLISM).isPresent()) {
            this.metabolism = entity.get(EntityComponentTypes.METABOLISM).get();
        } else {
            throw new IllegalArgumentException("Tried to attach a mood component to an ecs of class " + entity.getClass() + ". The given component must also have the metabolism component.");
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("mood", mood.name());
        compound.setInteger("currentMood", currentMood);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        mood = MoodType.valueOf(compound.getString("mood"));
        currentMood = compound.getInteger("currentMood");
    }

    public void update() {
        if (currentMood <= 100 && currentMood >= 0) {
            if (metabolism.food > foodThreshold || metabolism.water > waterThreshold) {
                currentMood++;
            } else {
                currentMood--;
            }
        }
        if (currentMood > mood.getThreshold()) {
            mood = MoodType.HAPPY;
        } else {
            mood = MoodType.ANGRY;
        }
    }

    @Getter
    public enum MoodType {
        HAPPY(50), ANGRY(50);

        private int threshold;

        MoodType(int threshold) {
            this.threshold = threshold;
        }
    }
}
