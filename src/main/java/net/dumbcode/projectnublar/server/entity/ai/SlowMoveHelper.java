package net.dumbcode.projectnublar.server.entity.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;

public class SlowMoveHelper extends LookController {

    public SlowMoveHelper(MobEntity entity) {
        super(entity);
    }

    @Override
    protected float rotateTowards(float sourceAngle, float targetAngle, float maximumChange) {
        return super.rotateTowards(sourceAngle, targetAngle, Math.min(maximumChange, 20F));
    }
}
