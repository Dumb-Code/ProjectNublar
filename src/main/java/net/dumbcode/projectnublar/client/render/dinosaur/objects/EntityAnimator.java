package net.dumbcode.projectnublar.client.render.dinosaur.objects;

import lombok.val;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityAnimatable;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class EntityAnimator implements ITabulaModelAnimator<DinosaurEntity> {
    protected EnumMap<GrowthStage, Map<DinosaurEntity, AnimationPassesWrapper<DinosaurEntity>>> animationHandlers = new EnumMap<>(GrowthStage.class);

    private AnimationPassesWrapper<DinosaurEntity> getAnimationHelper(DinosaurEntity entity, TabulaModel model, boolean useInertialTweens) {
        GrowthStage growth = entity.getGrowthStage();
        return this.animationHandlers.computeIfAbsent(growth, g -> new WeakHashMap<>()).computeIfAbsent(entity, e -> e.getDinosaur().getModelContainer().getPoseHandler().createPass(e, model, growth, useInertialTweens));
    }

    @Override
    public final void setRotationAngles(TabulaModel model, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
        this.getAnimationHelper(entity, model, true).performAnimations(entity, limbSwing, limbSwingAmount, ticks);
        this.performAnimations(model, entity, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
    }

    protected void performAnimations(TabulaModel parModel, DinosaurEntity entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
    }
}