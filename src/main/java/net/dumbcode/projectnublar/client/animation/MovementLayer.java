package net.dumbcode.projectnublar.client.animation;

import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;

import java.util.function.Function;

public class MovementLayer extends AnimationLayer<DinosaurEntity, ModelStage> {


    public MovementLayer(DinosaurEntity entity, ModelStage stage, TabulaModel model, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<ModelStage, DinosaurEntity> info, boolean inertia) {
        super(entity, stage, model, cuberef, info, inertia);
    }

    @Override
    public Animation<ModelStage> getAnimation() {
        if(this.isMoving(this.getEntity())) {
            return EnumAnimation.WALKING.get();
        }
        return EnumAnimation.IDLE.get();
    }

    private boolean isMoving(DinosaurEntity entity) {
        float deltaX = (float) (entity.posX - entity.prevPosX);
        float deltaZ = (float) (entity.posZ - entity.prevPosZ);
        return deltaX * deltaX + deltaZ * deltaZ > 0.001F;
    }

    @Override
    public boolean loop() {
        return true;
    }
}
