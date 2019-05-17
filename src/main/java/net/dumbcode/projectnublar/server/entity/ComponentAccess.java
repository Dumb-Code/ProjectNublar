package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public interface ComponentAccess {
    @Nullable
    <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type);

    @Nonnull
    <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrExcept(EntityComponentType<T, S> type);

    @Nonnull
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> Optional<T> get(EntityComponentType<T, S> type) {
        return Optional.ofNullable(this.getOrNull(type));
    }

    default boolean contains(EntityComponentType<?, ?> type) {
        return this.getOrNull(type) != null;
    }

    default boolean matchesAll(EntityComponentType<?, ?>... types) {
        for (EntityComponentType<?, ?> type : types) {
            if (!this.contains(type)) {
                return false;
            }
        }
        return true;
    }
}
