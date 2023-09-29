package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComponentWriteAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableAdditiveComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;

public class DinosaurCompatComponent extends EntityComponent implements FinalizableAdditiveComponent {
    @Override
    public void finalizeAdditiveComponent(ComponentAccess entity) {
        entity.get(ComponentHandler.DINOSAUR).ifPresent(dinosaurComponent -> {
            if(entity instanceof ComponentWriteAccess) {
                for (EntityComponentAttacher.ComponentPair<?, ?> pair : dinosaurComponent.getDinosaur().getAttacher().getDefaultConfig().getTypes()) {
                    if(!entity.matchesAll(pair.getType())) {
                        ProjectNublar.LOGGER.info("Attaching un-found component {}", pair.getType().getIdentifier());
                        pair.attach((ComponentWriteAccess) entity);
                    }
                }
            }
        });
    }
}
