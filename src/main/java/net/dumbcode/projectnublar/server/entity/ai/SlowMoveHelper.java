package net.dumbcode.projectnublar.server.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityMoveHelper;

public class SlowMoveHelper extends EntityMoveHelper {
    public SlowMoveHelper(EntityLiving entitylivingIn) {
        super(entitylivingIn);
    }

    @Override
    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
        return super.limitAngle(sourceAngle, targetAngle, Math.min(maximumChange, 20F));
    }
}
