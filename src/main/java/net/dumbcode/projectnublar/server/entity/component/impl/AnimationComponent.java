package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.TabulaUtils;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AnimationComponent<E extends Entity, N extends IStringSerializable> implements EntityComponent {

    @Getter private Animation<ModelStage> animation = EnumAnimation.IDLE.get();
    public AnimationRunWrapper<E, N> animationWrapper = null;

    public Function<E, ResourceLocation> modelGetter;
    public AnimationSystemInfo<N, E> info;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound; //TODO: serialize the animation ?
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
    }

    public void setAnimation(DinosaurEntity entity, Animation<ModelStage> animation) {
        this.animation = animation;
        if (this.animationWrapper != null) {
            for (AnimationLayer<E, ?> layer : this.animationWrapper.getLayers()) {
                layer.animate(entity.ticksExisted);
            }
        }
        if(!entity.world.isRemote) {
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation(entity, entity.getDinosaur().getSystemInfo(), animation), entity.world.provider.getDimension());
        }
    }

    public void createServersideWrapper(E entity) {
        if(this.modelGetter != null) {
            Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(this.modelGetter.apply(entity));
            List<AnimationLayer<E, N>> layers = Lists.newArrayList();
            for (PoseHandler.AnimationLayerFactory<E, N> factory : this.info.createFactories()) {
                Map<String, AnimationRunWrapper.CubeWrapper> map = new HashMap<>();
                layers.add(factory.createWrapper(entity, this.info.getStageFromEntity(entity), cubes.keySet(), cubes::get, s -> map.computeIfAbsent(s, o -> new AnimationRunWrapper.CubeWrapper(cubes.get(o))), this.info, true));
            }
            this.animationWrapper = new AnimationRunWrapper<>(entity, layers);
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<AnimationComponent> {

        private AnimationSystemInfo info;
        private Function<? extends Entity, ResourceLocation> modelGetter;

        @Override
        public AnimationComponent construct() {
            AnimationComponent component = new AnimationComponent<>();
            component.info = Objects.requireNonNull(this.info, "Info not set at construction");
            component.modelGetter = Objects.requireNonNull(this.modelGetter, "No way to get model has been set at construction");
            return component;
        }
    }
}
