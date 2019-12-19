package net.dumbcode.projectnublar.server.tablet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class TabletModuleType<S extends TabletModuleStorage<S>> extends IForgeRegistryEntry.Impl<TabletModuleType<?>> {
    private final Supplier<S> storageCreator;


    @SuppressWarnings("unchecked")
    public static Class<TabletModuleType<?>> getWildcardType() {
        return (Class<TabletModuleType<?>>) (Class<?>) TabletModuleType.class;
    }
}
