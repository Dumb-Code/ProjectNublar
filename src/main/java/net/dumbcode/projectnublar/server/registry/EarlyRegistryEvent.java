package net.dumbcode.projectnublar.server.registry;

import lombok.Getter;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Getter
public class EarlyRegistryEvent<T extends IForgeRegistryEntry<T>> extends GenericEvent<T>  {
    private final IForgeRegistry<T> registry;

    public EarlyRegistryEvent(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    public RegistryEvent.Register<T> asEvent() {
        return new RegistryEvent.Register<>(this.registry.getRegistryName(), this.registry);
    }
}
