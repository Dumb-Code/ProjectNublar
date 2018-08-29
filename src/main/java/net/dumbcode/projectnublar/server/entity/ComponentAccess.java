package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;

import javax.annotation.Nullable;

public interface ComponentAccess {
    @Nullable
    <T extends EntityComponent> T getOrNull(EntityComponentType<T> type);
}
