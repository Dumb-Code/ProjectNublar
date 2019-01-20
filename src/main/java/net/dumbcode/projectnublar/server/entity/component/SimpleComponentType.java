package net.dumbcode.projectnublar.server.entity.component;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class SimpleComponentType<T extends EntityComponent> implements EntityComponentType<T> {
    @ToString.Exclude private final Supplier<T> constructor;
    @EqualsAndHashCode.Include private final ResourceLocation identifier;
    private final Class<T> type;

    private SimpleComponentType(Supplier<T> constructor, ResourceLocation identifier, Class<T> type) {
        this.constructor = constructor;
        this.identifier = identifier;
        this.type = type;
    }

    public static <T extends EntityComponent> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    @Nonnull
    @Override
    public T construct() {
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityComponentType && ((EntityComponentType) obj).getIdentifier().equals(this.identifier);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleComponentType{identifier=" + this.identifier + ", type=" + this.type + "}";
    }

    public static class Builder<T extends EntityComponent> {
        private final Class<T> type;
        private Supplier<T> constructor;
        private ResourceLocation identifier;

        private Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> withConstructor(Supplier<T> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder<T> withIdentifier(ResourceLocation identifier) {
            this.identifier = identifier;
            return this;
        }

        public EntityComponentType<T> build() {
            Preconditions.checkNotNull(this.identifier, "Component identifier must be set");
            if(this.constructor == null) {
                ProjectNublar.getLogger().warn("No constructor set, trying to set to empty constructor of type " + this.type.getName());
                Constructor<T> constructor = ReflectionHelper.findConstructor(this.type);
                this.constructor = () -> {
                    try {
                        return constructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("Unable to construct component of class " + this.type.getName() + ", with component type " + this.identifier, e);
                    }
                };
            }
            return new SimpleComponentType<>(this.constructor, this.identifier, this.type);
        }
    }
}
