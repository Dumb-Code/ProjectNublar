package net.dumbcode.projectnublar.client.render.animator;

import net.dumbcode.dumblibrary.server.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.EntityAnimator;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;

public class DinosaurAnimator extends EntityAnimator<DinosaurEntity, ModelStage> {


    public DinosaurAnimator(PoseHandler<DinosaurEntity, ModelStage> poseHandler) {
        super(poseHandler);
    }

    @Override
    protected void performAnimations(TabulaModel parModel, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {

        super.performAnimations(parModel, entity, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
    }


}
