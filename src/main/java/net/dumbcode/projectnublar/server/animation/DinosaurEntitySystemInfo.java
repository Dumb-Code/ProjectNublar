package net.dumbcode.projectnublar.server.animation;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.client.animation.MovementLayer;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DinosaurEntitySystemInfo implements AnimationSystemInfo<ModelStage, DinosaurEntity> {
    private final Dinosaur dinosaur;

    public DinosaurEntitySystemInfo(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
        this.dinosaur.setSystemInfo(this);
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
        return new DinosaurAnimator(poseHandler);
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

    @Override
    public ModelContainer<DinosaurEntity, ModelStage> getModelContainer(DinosaurEntity entity) {
        return entity.getDinosaur().getModelContainer();
    }

    @Override
    public ModelStage getStageFromEntity(DinosaurEntity entity) {
        return entity.getModelStage();
    }

    @Override
    public ResourceLocation getTexture(DinosaurEntity entity) {
        return entity.getDinosaur().getTextureLocation(entity);
    }

    @Override
    public ResourceLocation identifier() {
        return new ResourceLocation(ProjectNublar.MODID, this.dinosaur.getFormattedName() + "_info");
    }
}
