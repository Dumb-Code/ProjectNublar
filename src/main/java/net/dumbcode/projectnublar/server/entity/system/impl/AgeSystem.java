package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;

import java.util.Map;

public enum AgeSystem implements EntitySystem {
    INSTANCE;

    private DinosaurComponent[] dinosaurs = new DinosaurComponent[0];
    private AgeComponent[] ages = new AgeComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.DINOSAUR, EntityComponentTypes.AGE);
        this.dinosaurs = family.populateBuffer(EntityComponentTypes.DINOSAUR);
        this.ages = family.populateBuffer(EntityComponentTypes.AGE);
    }

    @Override
    public void update() {
        for (int i = 0; i < this.dinosaurs.length; i++) {
            Map<ModelStage, Integer> ageTickMap = this.dinosaurs[i].dinosaur.getEntityProperties().getTickStageMap();
            AgeComponent age = this.ages[i];


            int ageoff = 0;
            for (int j = 0; j < ModelStage.values().length; j++) {
                ModelStage stage = ModelStage.values()[j];
                int ticks = ageTickMap.get(stage);
                if(ageoff < age.ageInTicks && ageoff + ticks >= age.ageInTicks) {
                    age.stage = stage;
                    age.percentageStage = (age.ageInTicks - ageoff) / (float)ticks;
                    break;
                }
                ageoff += ticks;
            }

        }
    }
}
