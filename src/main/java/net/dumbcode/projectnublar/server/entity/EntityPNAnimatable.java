package net.dumbcode.projectnublar.server.entity;

import net.minecraft.entity.Entity;
import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;

public interface EntityPNAnimatable extends EntityAnimatable {
    default boolean isMoving() {
        Entity entity = (Entity)this;
        float deltaX = (float) (entity.posX - entity.prevPosX);
        float deltaZ = (float) (entity.posZ - entity.prevPosZ);
        return deltaX * deltaX + deltaZ * deltaZ > 0.001F;
    }

    default boolean isClimbing() {
        return false;
    }

    default boolean isSwimming() {
        Entity entity = (Entity)this;
        return (entity.isInWater() || entity.isInLava()) && !entity.onGround;
    }

    default boolean inWater() {
        return ((Entity)this).isInWater();
    }

    default boolean inLava() {
        return ((Entity)this).isInLava();
    }

    boolean isRunning();
}
