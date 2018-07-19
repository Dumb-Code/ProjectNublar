package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurAnimations;
import net.dumbcode.projectnublar.client.render.dinosaur.PoseHandler;
import net.dumbcode.projectnublar.server.entity.EntityAnimatable;
import net.ilexiconn.llibrary.server.animation.Animation;

import java.util.List;
import java.util.Map;

public class MovementAnimationPass<T extends EntityAnimatable> extends AnimationPass<T> {

    public MovementAnimationPass(Map<Animation, List<PoseHandler.ModelData>> animations, Map<String, Map<String, CubeReference>> poses, boolean useInertia) {
        super(animations, poses, useInertia);
    }

    @Override
    protected boolean isEntityAnimationDependent() {
        return false;
    }

    @Override
    protected float getAnimationSpeed(T entity) {
        return entity.isMoving() ? this.getAnimationDegree(entity) : 3.0F;
    }

    @Override
    protected float getAnimationDegree(T entity) {
        float degree;
        if (this.animation == DinosaurAnimations.WALKING.get() || this.animation == DinosaurAnimations.RUNNING.get() || this.animation == DinosaurAnimations.SWIMMING.get() || this.animation == DinosaurAnimations.CLIMBING.get()) {
            if (entity.inWater() || entity.inLava()) {
                degree = this.limbSwingAmount * 4.0F;
            } else {
                degree = this.limbSwingAmount;
            }
        } else {
            return super.getAnimationDegree(entity);
        }

        return Math.max(entity.isMoving() ? 0.5F : 0.0F, Math.min(1.0F, degree));
    }

    @Override
    protected Animation getRequestedAnimation(T entity) {
        if (entity.isClimbing()) {
            return DinosaurAnimations.CLIMBING.get();
        } else if (entity.isMoving()) {
            if (entity.isSwimming()) {
                return this.animations.containsKey(DinosaurAnimations.SWIMMING.get()) ? DinosaurAnimations.SWIMMING.get() : DinosaurAnimations.WALKING.get();
            } else {
                if (entity.isRunning()) {
                    return DinosaurAnimations.RUNNING.get();
                } else {
                    return DinosaurAnimations.WALKING.get();
                }
            }
        } else {
            return DinosaurAnimations.IDLE.get();
        }
    }

    @Override
    public boolean isLooping() {
        return true;
    }
}