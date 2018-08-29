package net.dumbcode.projectnublar.server.entity.component;

import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterComponentEvent extends Event {
    @Getter
    private final IForgeRegistry<EntityComponentType<?>> registry;

    public RegisterComponentEvent(IForgeRegistry<EntityComponentType<?>> registry) {
        this.registry = registry;
    }
}
