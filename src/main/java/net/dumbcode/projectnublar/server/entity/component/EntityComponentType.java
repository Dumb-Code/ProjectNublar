package net.dumbcode.projectnublar.server.entity.component;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public interface EntityComponentType<T extends EntityComponent> {
    @Nonnull
    T construct();

    @Nonnull
    ResourceLocation getIdentifier();

    @Nonnull
    Class<T> getType();
}
