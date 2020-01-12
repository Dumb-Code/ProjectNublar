package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public enum MetabolismSystem implements EntitySystem {
    INSTANCE;
    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.METABOLISM);
        this.metabolism = family.populateBuffer(ComponentHandler.METABOLISM, this.metabolism);
        this.entities = family.getEntities();
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.metabolism.length; i++) {
            if(this.entities[i].ticksExisted % 20 == 0) {
                MetabolismComponent meta = this.metabolism[i];
                meta.food -= meta.foodRate;
                meta.water -= meta.waterRate;

                // TODO: Hurt the ecs when it has no more food / water left.
            }
        }
    }


}