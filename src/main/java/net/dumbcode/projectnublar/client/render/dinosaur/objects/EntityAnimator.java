package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class EntityAnimator implements ITabulaModelAnimator<DinosaurEntity> {
    protected EnumMap<GrowthStage, Map<DinosaurEntity, AnimationPass>> animationHandlers = new EnumMap<>(GrowthStage.class);

    private AnimationPass getAnimationHelper(DinosaurEntity entity, TabulaModel model, boolean useInertialTweens) {
        GrowthStage growth = GrowthStage.ADULT; //TODO
        Map<DinosaurEntity, AnimationPass> growthToRender = this.animationHandlers.get(growth);

        if (growthToRender == null) {
            growthToRender = new WeakHashMap<>();
            this.animationHandlers.put(growth, growthToRender);
        }

        AnimationPass render = growthToRender.get(entity);

        if (render == null) {
            render = entity.getDinosaur().getPoseHandler().createPass(entity, model, growth, useInertialTweens);
            growthToRender.put(entity, render);
        }

        return render;
    }

    @Override
    public final void setRotationAngles(TabulaModel model, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
        this.getAnimationHelper(entity, model, true).performAnimations(entity, ticks);
//        for(int i = 0;true;i++) {
//            AdvancedModelRenderer cube = model.getCube("neck" + i++);
//            if(cube == null) {
//                cube = model.getCube("throat" + i++);
//            }
//            float j = 1 - (i * 0.00001F);
//            if(cube != null ) {
//                cube.scaleX *= j;
//                cube.scaleY *= j;
//                cube.scaleZ *= j;
//
//            }
//            break;
//        }
        this.performAnimations(model, entity, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
    }

    protected void performAnimations(TabulaModel parModel, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
    }
}