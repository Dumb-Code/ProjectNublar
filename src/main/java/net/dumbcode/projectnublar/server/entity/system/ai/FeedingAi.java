package net.dumbcode.projectnublar.server.entity.system.ai;

import net.dumbcode.dumblibrary.server.ai.AdvancedAIBase;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.EntityLiving;

public class FeedingAi extends AdvancedAIBase {
    private final MetabolismComponent metabolism;
    public FeedingAi(EntityLiving entity, MetabolismComponent metabolism) {
        super(entity);
        this.metabolism = metabolism;
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void checkImportance() {

    }
}
