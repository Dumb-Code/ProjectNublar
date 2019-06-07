package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnimationComponent<E extends Entity> implements EntityComponent {

    @Getter private Animation animation;
    public AnimationRunWrapper<E> animationWrapper = null;

    public ModelGetter<E> modelGetter;
    public List<ModelStage> modelGrowthStages;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("model_getter_id", this.modelGetter.registryName.toString());
        compound.setTag("model_getter", this.modelGetter.serialize(new NBTTagCompound()));
        return compound; //TODO: serialize the animation ?
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(NBTTagCompound compound) {
        this.modelGetter = modelGetterRegistry.get(new ResourceLocation(compound.getString("model_getter_id")));
        this.modelGetter.deserialize(compound.getCompoundTag("model_getter"));
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.modelGetter.registryName.toString());
        this.modelGetter.serialize(buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(ByteBuf buf) {
        this.modelGetter = modelGetterRegistry.get(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
        this.modelGetter.deserialize(buf);
    }

    @SuppressWarnings("unchecked")
    public void setAnimation(ComponentAccess entity, Animation animation) {
        Entity e = (Entity) entity;
        this.animation = animation;
        if (this.animationWrapper != null) {
            for (AnimationLayer<E> layer : this.animationWrapper.getLayers()) {
                layer.animate(e.ticksExisted);
            }
        }
        if(!e.world.isRemote) {
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation((E) entity, this.modelGetter.getInfo((E) entity), animation), e.world.provider.getDimension());
        }
    }

    public void createServersideWrapper(E entity) {
        if(this.modelGetter != null) {
            Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(this.modelGetter.getLocation(entity));
            List<AnimationLayer<E>> layers = Lists.newArrayList();
            for (ModelContainer.AnimationLayerFactory<E> factory : this.modelGetter.getInfo(entity).createFactories()) {
                Map<String, AnimationRunWrapper.CubeWrapper> map = new HashMap<>();
                layers.add(factory.createWrapper(entity, cubes.keySet(), cubes::get, s -> map.computeIfAbsent(s, o -> new AnimationRunWrapper.CubeWrapper(cubes.get(o))), this.modelGetter.getInfo(entity), true));
            }
            this.animationWrapper = new AnimationRunWrapper<>(entity, layers);
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<AnimationComponent> {

        @Getter private List<ModelStage> modelGrowthStages = Lists.newArrayList(ModelStage.ADULT, ModelStage.SKELETON);
        private ModelGetter modelGetter;

        @Override
        public AnimationComponent construct() {
            AnimationComponent component = new AnimationComponent<>();
            component.modelGrowthStages = modelGrowthStages;
            component.modelGetter = Objects.requireNonNull(this.modelGetter, "No way to get model has been set at construction");
            return component;
        }
    }

    public static Map<ResourceLocation, ModelGetter> modelGetterRegistry = Maps.newHashMap();

    public abstract static class ModelGetter<E extends Entity> {

        final ResourceLocation registryName;

        public ModelGetter(ResourceLocation registryName) {
            this.registryName = registryName;
            modelGetterRegistry.put(this.registryName, this);
        }

        public abstract ResourceLocation getLocation(E entity);

        public abstract AnimationSystemInfo<E> getInfo(E entity);

        public NBTTagCompound serialize(NBTTagCompound compound) {
            return compound;
        }

        public void deserialize(NBTTagCompound compound) {
        }

        public void serialize(ByteBuf buf) {
        }

        public void deserialize(ByteBuf buf) {
        }
    }
}
