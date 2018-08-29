package net.dumbcode.projectnublar.server.entity.component;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SimpleComponentType<T extends EntityComponent> implements EntityComponentType<T> {
    private final Supplier<T> constructor;
    private final ResourceLocation identifier;
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
    public Class<T> getType() {
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
            Preconditions.checkNotNull(this.constructor, "Component constructor must be set");
            Preconditions.checkNotNull(this.identifier, "Component identifier must be set");
            return new SimpleComponentType<>(this.constructor, this.identifier, this.type);
        }
    }
}
