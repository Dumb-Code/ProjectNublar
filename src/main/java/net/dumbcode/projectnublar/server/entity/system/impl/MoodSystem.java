package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MoodSystem implements EntitySystem {

    private Entity[] entities = new Entity[0];
    private MoodComponent[] components = new MoodComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.MOOD);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(ComponentHandler.MOOD, this.components);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.components.length; i++) {
            ComponentAccess access = (ComponentAccess) this.entities[i];
            this.components[i].getEntries().values().forEach(v -> v.runIfDirty(access));
        }
    }
}
