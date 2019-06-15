package net.dumbcode.projectnublar.server.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.EntityAnimator;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.PoseData;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

public class DinosaurEntitySystemInfo implements ComponentAnimationSystemInfo<DinosaurEntity> {
    private final Dinosaur dinosaur;
    private final ModelStage stage;

    private final Map<Animation, List<PoseData>> animationList = Maps.newHashMap();

    public DinosaurEntitySystemInfo(Dinosaur dinosaur, ModelStage stage) {
        this.dinosaur = dinosaur;
        this.stage = stage;
    }


    @Override
    public EntityAnimator<DinosaurEntity> createAnimator(ModelContainer<DinosaurEntity> modelContainer) {
        return new EntityAnimator<>(modelContainer);
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
    public void setPoseData(Animation animation, List<PoseData> poseData) {
        this.animationList.put(animation, poseData);
    }

    @Override
    public List<PoseData> getPoseData(Animation animation) {
        return this.animationList.getOrDefault(animation, Lists.newArrayList());
    }


}
