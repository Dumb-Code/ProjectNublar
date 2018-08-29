package net.dumbcode.projectnublar.server.entity.system;

import net.dumbcode.projectnublar.server.entity.EntityManager;

public interface EntitySystem {
    void populateBuffers(EntityManager manager);

    void update();
}
