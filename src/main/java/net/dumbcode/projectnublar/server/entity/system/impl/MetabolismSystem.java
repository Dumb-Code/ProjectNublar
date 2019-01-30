package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;

public class MetabolismSystem implements EntitySystem
{

    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];

    @Override
    public void populateBuffers(EntityManager manager)
    {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.METABOLISM, EntityComponentTypes.DINOSAUR);
        metabolism = family.populateBuffer(EntityComponentTypes.METABOLISM, metabolism);
        dinosaurs = family.populateBuffer(EntityComponentTypes.DINOSAUR);
    }

    @Override
    public void update()
    {
        for (int i = 0; i < metabolism.length; i++)
        {
        }
    }
}
