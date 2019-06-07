package net.dumbcode.projectnublar.server.animation;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.animation.PoseHandler;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.client.animation.EntityAnimator;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.client.animation.MovementLayer;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DinosaurEntitySystemInfo implements AnimationSystemInfo<ModelStage, DinosaurEntity> {
    private final Dinosaur dinosaur;

    public DinosaurEntitySystemInfo(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }

    @Override
    public ModelStage defaultStage() {
        return ModelStage.ADULT;
    }

    @Override
    public List<ModelStage> allAcceptedStages() {
        return this.dinosaur.getModelProperties().getModelGrowthStages();
    }

    @Override
    public ModelStage[] allValues() {
        return ModelStage.values();
    }

    @Override
    public Map<ModelStage, String> stageToModelMap() {
        return this.dinosaur.getModelProperties().getMainModelMap();
    }

    @Override
    public Collection<String> allAnimationNames() {
        return EnumAnimation.getNames();
    }

    @Override
    public EntityAnimator<DinosaurEntity, ModelStage> createAnimator(PoseHandler<DinosaurEntity, ModelStage> poseHandler) {
        return new EntityAnimator<>(poseHandler);
    }

    @Override
    public Animation<ModelStage> getAnimation(String animation) {
        return EnumAnimation.fromName(animation);
    }

    @Override
    public Animation<ModelStage> defaultAnimation() {
        return EnumAnimation.IDLE.get();
    }

    @Override
    public List<PoseHandler.AnimationLayerFactory<DinosaurEntity, ModelStage>> createFactories() {
        return Lists.newArrayList(
                AnimationLayer::new,
                MovementLayer::new
        );
    }

    @Nonnull
    @Override
    public Animation<ModelStage> getAnimation(DinosaurEntity entity) {
        AnimationComponent comp = entity.getOrNull(EntityComponentTypes.ANIMATION);
        return comp == null ? this.defaultAnimation() : comp.getAnimation();
    }

    @Override
    public void setAnimation(DinosaurEntity entity, @Nonnull Animation<ModelStage> animation) {
        AnimationComponent comp = entity.getOrNull(EntityComponentTypes.ANIMATION);
        if (comp != null) {
            comp.setAnimation(entity, animation);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelContainer<DinosaurEntity, ModelStage> getModelContainer(DinosaurEntity entity) {
        return entity.getDinosaur().getModelContainer();
    }

    @Override
    public ModelStage getStageFromEntity(DinosaurEntity entity) {
        return entity.get(EntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(ModelStage.ADULT);
    }

    @Override
    public ResourceLocation getTexture(DinosaurEntity entity) {
        return entity.getDinosaur().getTextureLocation(entity);
    }

    @Override
    public ResourceLocation identifier() {
        return new ResourceLocation(ProjectNublar.MODID, this.dinosaur.getFormattedName() + "_info");
    }

    @Override
    public AnimationRunWrapper<DinosaurEntity, ModelStage> getOrCreateWrapper(DinosaurEntity entity, PoseHandler<DinosaurEntity, ModelStage> poseHandler, TabulaModel model, boolean inertia) {
        @SuppressWarnings("unchecked")
        AnimationComponent<DinosaurEntity, ModelStage> component = entity.getOrNull(EntityComponentTypes.ANIMATION);
        if(component != null) {
            if(component.animationWrapper == null) {
                component.animationWrapper = poseHandler.createAnimationWrapper(entity, model, this.getStageFromEntity(entity), inertia, this.createFactories());
            }
            return component.animationWrapper;
        }
        throw new IllegalArgumentException("Tried to get animation wrapper from entity with no animation component " + entity);
    }
}
