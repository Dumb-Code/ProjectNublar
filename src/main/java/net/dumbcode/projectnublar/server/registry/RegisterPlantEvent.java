package net.dumbcode.projectnublar.server.registry;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterPlantEvent extends Event {

    @Getter
    private final IForgeRegistry<Plant> registry;

    public RegisterPlantEvent(IForgeRegistry<Plant> registry) {
        this.registry = registry;
    }
}
