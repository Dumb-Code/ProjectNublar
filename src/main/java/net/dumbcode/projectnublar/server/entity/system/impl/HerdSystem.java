package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.HerdComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;

public class HerdSystem implements EntitySystem {

    private Entity[] matchedEntities = new Entity[0];
    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];
    private HerdComponent[] herds = new HerdComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.HERD, EntityComponentTypes.DINOSAUR);
        family.populateBuffer(EntityComponentTypes.HERD, this.herds);
        family.populateBuffer(EntityComponentTypes.DINOSAUR, this.dinosaurs);
        this.matchedEntities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.herds.length; i++) {
            Entity e = this.matchedEntities[i];
            DinosaurComponent dino = this.dinosaurs[i];
            HerdComponent herd = this.herds[i];

            if(herd.leader) {

            }
        }
    }
}
