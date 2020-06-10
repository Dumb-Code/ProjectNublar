package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.AxisAlignedBB;

public class FloatAi extends EntityAIBase {
    private final EntityLiving entity;

    public FloatAi(EntityLiving entityIn) {
        this.entity = entityIn;
        this.setMutexBits(4);

        if (entityIn.getNavigator() instanceof PathNavigateGround) {
            ((PathNavigateGround)entityIn.getNavigator()).setCanSwim(true);
        } else if (entityIn.getNavigator() instanceof PathNavigateFlying) {
            ((PathNavigateFlying)entityIn.getNavigator()).setCanFloat(true);
        }
    }

    @Override
    public boolean shouldExecute() {
        return this.entity.isInWater() || this.entity.isInLava();
    }

    @Override
    public void updateTask() {
        for (Material material : new Material[]{Material.WATER, Material.LAVA}) {
            AxisAlignedBB box = this.entity.getEntityBoundingBox();
            if (this.entity.world.handleMaterialAcceleration(box.contract(0, box.minY-box.maxY, 0).shrink(0.001D), material, this.entity)) {
                if (this.entity.getRNG().nextFloat() < 0.8F) {
                    this.entity.motionY =+ 0.3;
                    return;
                }
            }
        }
        if (this.entity.onGround) {
            this.entity.motionY =+ 0.3;
        }

    }
}
