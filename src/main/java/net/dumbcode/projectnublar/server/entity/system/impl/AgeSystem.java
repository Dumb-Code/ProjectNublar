package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.minecraft.entity.Entity;

import java.util.Iterator;

public enum AgeSystem implements EntitySystem {
    INSTANCE;
    private AgeComponent[] ages = new AgeComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(ComponentHandler.AGE);
        this.ages = family.populateBuffer(ComponentHandler.AGE);
        this.entities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.ages.length; i++) {
            AgeComponent age = this.ages[i];
            AgeStage start = age.stage;
            int ageoff = age.ageInTicks;

            Iterator<AgeStage> iterator = age.orderedAges.iterator();
            while(ageoff > 0 && iterator.hasNext()) {
                age.stage = iterator.next();
                ageoff -= age.stage.getTime();
                if(age.stage.getTime() == -1) {
                    break;
                }
            }


            if(age.stage == null) {
                age.percentageStage = 0F;
            } else {
                age.percentageStage = (ageoff + age.stage.getTime()) / (float) age.stage.getTime();
            }

            if(start != age.stage) {
                Entity entity = this.entities[i];
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).finalizeComponents();
                }
            }

            age.ageInTicks++;
        }
    }
}
