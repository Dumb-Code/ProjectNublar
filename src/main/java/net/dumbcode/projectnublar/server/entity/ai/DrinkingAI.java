package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.projectnublar.server.utils.AIUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class DrinkingAI extends EntityAIBase {

    private EntityCreature entity;
    private BlockPos pos;

    public DrinkingAI(EntityCreature entity, float speed) {
        this.entity = entity;
        this.entity.setAIMoveSpeed(speed);
    }

    @Override
    public boolean shouldExecute() {
        List<BlockPos> pos = AIUtils.traverseXZ((int) entity.posX, (int) entity.posY - 1, (int) entity.posZ, 10);
        for (BlockPos bPos : pos) {
            if (entity.world.getBlockState(bPos).getMaterial() == Material.WATER) {
                this.pos = bPos;
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        entity.getNavigator().tryMoveToXYZ(pos.getX() + 1, pos.getY(), pos.getZ(), entity.getAIMoveSpeed());
    }

    @Override
    public boolean shouldContinueExecuting() {
        if(entity.getNavigator().noPath()) {
            return this.shouldExecute();
        }
        return false;
    }
}