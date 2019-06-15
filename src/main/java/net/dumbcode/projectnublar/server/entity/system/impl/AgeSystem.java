package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.NublarEntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;

public enum AgeSystem implements EntitySystem {
    INSTANCE;
    private AgeComponent[] ages = new AgeComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(NublarEntityComponentTypes.AGE);
        this.ages = family.populateBuffer(NublarEntityComponentTypes.AGE);
    }

    @Override
    public void update() {
        for (int i = 0; i < this.ages.length; i++) {
            AgeComponent age = this.ages[i];
            int ageoff = 0;
            for (int j = 0; j < ModelStage.values().length; j++) {
                ModelStage stage = ModelStage.values()[j];
                int ticks = age.tickStageMap.computeIfAbsent(stage, s -> 36000); //Default at 30 minutes
                if(ageoff < age.ageInTicks && ageoff + ticks >= age.ageInTicks) {
                    age.stage = stage;
                    age.percentageStage = (age.ageInTicks - ageoff) / (float)ticks;
                    break;
                }
            }
            ageoff += age.tickStageMap.get(age.stage);

        }
    }
}
