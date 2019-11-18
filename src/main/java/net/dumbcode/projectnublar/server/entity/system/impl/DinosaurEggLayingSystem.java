package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.FamilyComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurEggLayingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.List;

public enum DinosaurEggLayingSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private DinosaurEggLayingComponent[] eggLayingComponents = new DinosaurEggLayingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.DINOSAUR_EGG_LAYING);
        this.entities = family.getEntities();
        this.eggLayingComponents = family.populateBuffer(ComponentHandler.DINOSAUR_EGG_LAYING, this.eggLayingComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity en = this.entities[i];
            ComponentAccess access = (ComponentAccess) en;
            DinosaurEggLayingComponent component = this.eggLayingComponents[i];
            component.getHeldEggs().removeIf(egg -> {
                int ticksLeft = egg.getTicksLeft();
                if(ticksLeft <= 0) {
                    DinosaurEntity child = access.getOrExcept(ComponentHandler.DINOSAUR).getDinosaur().createEntity(world);

                    child.setPosition(en.posX, en.posY, en.posZ);

                    List<GeneticEntry<?>> entries = child.getOrExcept(EntityComponentTypes.GENETICS).getGenetics();
                    entries.clear();

                    entries.addAll(egg.getCombinedGenetics());

                    access.get(EntityComponentTypes.FAMILY).flatMap(FamilyComponent::getDataCache).ifPresent(f -> f.getChildren().add(child.getUniqueID()));
                    child.get(ComponentHandler.AGE).ifPresent(a -> a.resetStageTo(Dinosaur.CHILD_AGE));

                    child.finalizeComponents();

                    world.spawnEntity(child);

                    return true;
                } else {
                    egg.setTicksLeft(ticksLeft - 1);
                }
                return false;
            });

        }
    }
}
