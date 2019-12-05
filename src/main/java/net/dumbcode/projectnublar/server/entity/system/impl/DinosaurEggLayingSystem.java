package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.FamilyComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEggEntity;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurEggLayingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public enum DinosaurEggLayingSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private DinosaurEggLayingComponent[] eggLayingComponents = new DinosaurEggLayingComponent[0];
    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.DINOSAUR_EGG_LAYING, ComponentHandler.DINOSAUR);
        this.entities = family.getEntities();
        this.eggLayingComponents = family.populateBuffer(ComponentHandler.DINOSAUR_EGG_LAYING, this.eggLayingComponents);
        this.dinosaurs = family.populateBuffer(ComponentHandler.DINOSAUR, this.dinosaurs);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity en = this.entities[i];
            ComponentAccess access = (ComponentAccess) en;
            DinosaurEggLayingComponent component = this.eggLayingComponents[i];
            DinosaurComponent dinosaur = this.dinosaurs[i];
            component.getHeldEggs().removeIf(egg -> {
                int ticksLeft = egg.getTicksLeft();
                if(ticksLeft <= 0) {
                    DinosaurEggEntity eggEntity = new DinosaurEggEntity(
                        en.world,
                        egg.getCombinedGenetics(),
                        dinosaur.getDinosaur(),
                        access.get(EntityComponentTypes.FAMILY).map(FamilyComponent::getFamilyUUID).orElse(null),
                        egg.getEggTicks()
                    );

                    eggEntity.setPosition(en.posX, en.posY, en.posZ);

                    world.spawnEntity(eggEntity);

                    return true;
                } else {
                    egg.setTicksLeft(ticksLeft - 1);
                }
                return false;
            });

        }
    }
}
