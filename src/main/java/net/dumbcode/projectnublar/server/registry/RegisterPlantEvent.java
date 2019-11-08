package net.dumbcode.projectnublar.server.registry;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegisterPlantEvent extends Event {
    private final IForgeRegistry<Plant> registry;
}
