package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ComponentAccess {
    @Nullable
    <T extends EntityComponent> T getOrNull(EntityComponentType<T> type);

    @Nonnull
    <T extends EntityComponent> T getOrExcept(EntityComponentType<T> type);

    default boolean contains(EntityComponentType<?> type) {
        return this.getOrNull(type) != null;
    }

    default boolean matchesAll(EntityComponentType<?>... types) {
        for (EntityComponentType<?> type : types) {
            if (!this.contains(type)) {
                return false;
            }
        }
        return true;
    }
}
