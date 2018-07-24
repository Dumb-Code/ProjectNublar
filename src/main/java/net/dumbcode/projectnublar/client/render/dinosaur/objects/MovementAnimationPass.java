package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import net.dumbcode.dumblibrary.client.animation.AnimationInfo;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationPass;
import net.dumbcode.dumblibrary.client.animation.objects.CubeReference;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.EntityPNAnimatable;
import net.ilexiconn.llibrary.server.animation.Animation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MovementAnimationPass extends AnimationPass<EntityPNAnimatable> {

    public MovementAnimationPass(Animation defaultAnimation, Map<Animation, List<PoseHandler.PoseData>> animations, Map<String, Map<String, CubeReference>> poses, Function<Animation, AnimationInfo> animationInfoGetter, boolean useInertia) {
        super(defaultAnimation, animations, poses, animationInfoGetter, useInertia);
    }

    @Override
    protected boolean isEntityAnimationDependent() {
        return false;
    }

    @Override
    protected float getAnimationSpeed(EntityPNAnimatable entity) {
        return entity.isMoving() ? this.getAnimationDegree(entity) : 3.0F;
    }

    @Override
    protected float getAnimationDegree(EntityPNAnimatable entity) {
        float degree;
        if (this.animation == EnumAnimation.WALKING.get() || this.animation == EnumAnimation.RUNNING.get() || this.animation == EnumAnimation.SWIMMING.get() || this.animation == EnumAnimation.CLIMBING.get()) {
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
    protected Animation getRequestedAnimation(EntityPNAnimatable entity) {
        if (entity.isClimbing()) {
            return EnumAnimation.CLIMBING.get();
        } else if (entity.isMoving()) {
            if (entity.isSwimming()) {
                return this.animations.containsKey(EnumAnimation.SWIMMING.get()) ? EnumAnimation.SWIMMING.get() : EnumAnimation.WALKING.get();
            } else {
                if (entity.isRunning()) {
                    return EnumAnimation.RUNNING.get();
                } else {
                    return EnumAnimation.WALKING.get();
                }
            }
        } else {
            return EnumAnimation.IDLE.get();
        }
    }

    @Override
    public boolean isLooping() {
        return true;
    }
}