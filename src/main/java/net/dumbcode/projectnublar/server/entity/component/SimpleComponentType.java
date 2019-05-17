package net.dumbcode.projectnublar.server.entity.component;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SimpleComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> implements EntityComponentType<T, S> {
    @ToString.Exclude private final Supplier<T> constructor;
    @Nullable @ToString.Exclude private final Supplier<S> storageConstructor;
    @EqualsAndHashCode.Include private final ResourceLocation identifier;
    private final Class<T> type;

    private SimpleComponentType(Supplier<T> constructor, @Nullable Supplier<S> storageConstructor, ResourceLocation identifier, Class<T> type) {
        this.constructor = constructor;
        this.identifier = identifier;
        this.type = type;
        this.storageConstructor = storageConstructor;
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> Builder<T, S> builder(Class<T> type) {
        return new Builder<>(type, null);
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> Builder<T, S> builder(Class<T> type, Class<S> storageType) {
        return new Builder<>(type, storageType);
    }

    @Nullable
    @Override
    public S constructStorage() {
        return this.storageConstructor == null ? null : this.storageConstructor.get();
    }

    @Nonnull
    @Override
    public T constructEmpty() {
        return this.constructor.get();
    }

    @Nonnull
    @Override
    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    @Nonnull
    @Override
    public Class<? extends T> getType() {
        return this.type;
    }


    public static class Builder<T extends EntityComponent, S extends EntityComponentStorage<T>> {
        private final Class<T> type;
        private final Class<S> storageType; //Used to infer types
        private Supplier<T> constructor;
        private Supplier<S> storageConstructor;
        private ResourceLocation identifier;

        private Builder(Class<T> type, Class<S> storageType) {
            this.type = type;
            this.storageType = storageType;
        }

        public Builder<T, S> withConstructor(Supplier<T> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder<T, S> withStorage(Supplier<S> storageConstructor) {
            this.storageConstructor = storageConstructor;
            return this;
        }

        public Builder<T, S> withIdentifier(ResourceLocation identifier) {
            this.identifier = identifier;
            return this;
        }

        public EntityComponentType<T, S> build() {
            Preconditions.checkNotNull(this.identifier, "Component identifier must be set");
            if(this.constructor == null) {
                ProjectNublar.getLogger().warn("No constructor set, trying to set to empty constructor of type " + this.type.getName());
                Constructor<T> constructor = ReflectionHelper.findConstructor(this.type);
                this.constructor = () -> {
                    try {
                        return constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("Unable to constructEmpty component of class " + this.type.getName() + ", with component type " + this.identifier, e);
                    }
                };
            }
            return new SimpleComponentType<>(this.constructor, this.storageConstructor, this.identifier, this.type);
        }
    }
}
