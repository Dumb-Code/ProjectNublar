package net.dumbcode.projectnublar.server.entity.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class FloatAi extends Goal {
    private final MobEntity entity;

    public FloatAi(MobEntity entityIn) {
        this.entity = entityIn;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        entityIn.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        return this.entity.isInWater() || this.entity.isInLava();
    }

    @Override
    public void tick() {
        AxisAlignedBB box = this.entity.getBoundingBox();
        for (BlockPos blockPos : BlockPos.betweenClosedStream(box.contract(0, box.minY - box.maxY, 0).deflate(0.001D)).collect(Collectors.toList())) {
            FluidState fluidState = this.entity.level.getFluidState(blockPos);
            if(!fluidState.isEmpty()) {
                if (this.entity.getRandom().nextFloat() < 0.8F) {
                    this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, 0.25, 0));
                    return;
                }
            }
        }
        if (this.entity.isOnGround() && this.entity.getRandom().nextFloat() < 0.8F) {
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0, 0.25, 0));
        }

    }
}
