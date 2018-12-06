package net.dumbcode.projectnublar.server.entity.system;

import lombok.Getter;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

public class RegisterSystemsEvent extends Event {
    @Getter
    private final World world;
    private final List<EntitySystem> systems;

    public RegisterSystemsEvent(World world, List<EntitySystem> systems) {
        this.world = world;
        this.systems = systems;
    }

    public void registerSystem(EntitySystem system) {
        this.systems.add(system);
    }
}
