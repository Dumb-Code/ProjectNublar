package net.dumbcode.projectnublar.server;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterDinosaurEvent extends Event {

    @Getter
    private final IForgeRegistry<Dinosaur> registry;

    public RegisterDinosaurEvent(IForgeRegistry<Dinosaur> registry) {
        this.registry = registry;
    }
}
