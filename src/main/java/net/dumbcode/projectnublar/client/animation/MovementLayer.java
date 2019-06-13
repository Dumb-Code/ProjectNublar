package net.dumbcode.projectnublar.client.animation;

import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;

import java.util.Collection;
import java.util.function.Function;

public class MovementLayer extends AnimationLayer<DinosaurEntity> {


    public MovementLayer(DinosaurEntity entity, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<DinosaurEntity> info, boolean inertia) {
        super(entity, cubeNames, anicubeRef, cuberef, info, inertia);
    }

    @Override
    public Animation getAnimation() {
        if(this.isMoving(this.getEntity())) {
            return EnumAnimation.IDLE.get();
        }
        return EnumAnimation.IDLE.get();
    }

    private boolean isMoving(DinosaurEntity entity) {
        float deltaX = (float) (entity.posX - entity.prevPosX);
        float deltaZ = (float) (entity.posZ - entity.prevPosZ);
        return deltaX * deltaX + deltaZ * deltaZ > 1.001F;
    }

    @Override
    public boolean loop() {
        return true;
    }
}
