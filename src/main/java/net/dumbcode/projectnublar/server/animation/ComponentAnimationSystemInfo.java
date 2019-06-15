package net.dumbcode.projectnublar.server.animation;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.animation.objects.MultiAnimationLayer;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.AnimationComponent;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface ComponentAnimationSystemInfo<E extends Entity & ComponentAccess> extends AnimationSystemInfo<E> {

    @Override
    default List<ModelContainer.AnimationLayerFactory<E>> createFactories() {
        return Lists.newArrayList();
    }

    @Nonnull
    @Override
    default Animation getAnimation(E entity) {
        return Animation.NONE;
    }

    @Override
    default AnimationRunWrapper<E> getOrCreateWrapper(E entity, ModelContainer<E> modelContainer, TabulaModel model, boolean inertia) {
        @SuppressWarnings("unchecked")
        AnimationComponent<E> component = entity.getOrNull(EntityComponentTypes.ANIMATION);
        if(component != null) {
            if(component.getAnimationWrapper() == null) {
                DelegateFactoryClass<E> delegate = new DelegateFactoryClass<>();
                AnimationRunWrapper<E> wrapper = modelContainer.createAnimationWrapper(entity, model, inertia, Lists.newArrayList(delegate));
                component.initAnimationData(wrapper, delegate.layer);
            }
            return component.getAnimationWrapper();
        }
        throw new IllegalArgumentException("Tried to get animation wrapper from entity with no animation component " + entity);
    }

    /**
     * Used to cache the result of the layer creation
     * @param <E>
     */
    @RequiredArgsConstructor
    class DelegateFactoryClass<E extends Entity & ComponentAccess> implements ModelContainer.AnimationLayerFactory<E> {

        private MultiAnimationLayer<E> layer;

        public AnimationLayer<E> createLayer(E entity, Collection<String> cubeNames, Function<String, AnimationLayer.AnimatableCube> anicubeRef, AnimationSystemInfo<E> info, boolean inertia) {
            return this.layer = new MultiAnimationLayer<>(entity, cubeNames, anicubeRef, info, inertia);
        }
    }
}
