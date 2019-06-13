package net.dumbcode.projectnublar.server.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.EntityAnimator;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.animation.objects.PoseData;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.client.animation.MovementLayer;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DinosaurEntitySystemInfo implements AnimationSystemInfo<DinosaurEntity> {
    private final Dinosaur dinosaur;
    private final ModelStage stage;

    private final Map<Animation, List<PoseData>> animationList = Maps.newHashMap();

    public DinosaurEntitySystemInfo(Dinosaur dinosaur, ModelStage stage) {
        this.dinosaur = dinosaur;
        this.stage = stage;
    }

    @Override
    public Collection<String> allAnimationNames() {
        return EnumAnimation.getNames();
    }

    @Override
    public EntityAnimator<DinosaurEntity> createAnimator(ModelContainer<DinosaurEntity> modelContainer) {
        return new EntityAnimator<>(modelContainer);
    }

    @Override
    public Animation getAnimation(String animation) {
        return EnumAnimation.fromName(animation);
    }

    @Override
    public Animation defaultAnimation() {
        return EnumAnimation.IDLE.get();
    }

    @Override
    public List<ModelContainer.AnimationLayerFactory<DinosaurEntity>> createFactories() {
        return Lists.newArrayList(
                AnimationLayer::new,
                MovementLayer::new
        );
    }


    @Nonnull
    @Override
    public Animation getAnimation(DinosaurEntity entity) {
        AnimationComponent comp = entity.getOrNull(EntityComponentTypes.ANIMATION);
        return comp == null ? this.defaultAnimation() : comp.getAnimation();
    }

    @Override
    public void setAnimation(DinosaurEntity entity, @Nonnull Animation animation) {
        AnimationComponent comp = entity.getOrNull(EntityComponentTypes.ANIMATION);
        if (comp != null) {
            comp.setAnimation(entity, animation);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelContainer<DinosaurEntity> getModelContainer(DinosaurEntity entity) {
        return entity.getDinosaur().getModelContainer().get(entity.getState());
    }

    @Override
    public ResourceLocation getTexture(DinosaurEntity entity) {
        return entity.getDinosaur().getTextureLocation(entity);
    }

    @Override
    public ResourceLocation identifier() {
        return new ResourceLocation(ProjectNublar.MODID, this.dinosaur.getFormattedName() + "." + this.stage.getName() + "_info");
    }

    @Override
    public AnimationRunWrapper<DinosaurEntity> getOrCreateWrapper(DinosaurEntity entity, ModelContainer<DinosaurEntity> modelContainer, TabulaModel model, boolean inertia) {
        @SuppressWarnings("unchecked")
        AnimationComponent<DinosaurEntity> component = entity.getOrNull(EntityComponentTypes.ANIMATION);
        if(component != null) {
            if(component.animationWrapper == null) {
                component.animationWrapper = modelContainer.createAnimationWrapper(entity, model, inertia, this.createFactories());
            }
            return component.animationWrapper;
        }
        throw new IllegalArgumentException("Tried to get animation wrapper from entity with no animation component " + entity);
    }

    @Override
    public void setPoseData(Animation animation, List<PoseData> poseData) {
        this.animationList.put(animation, poseData);
    }

    @Override
    public List<PoseData> getPoseData(Animation animation) {
        return this.animationList.getOrDefault(animation, Lists.newArrayList());
    }


}
