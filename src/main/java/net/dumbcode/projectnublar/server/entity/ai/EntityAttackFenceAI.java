package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.utils.AIUtils;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityAttackFenceAI extends Goal {

    private CreatureEntity entity;

    /**
     * Fence that was targeted.
     */
    private BlockPos fence;

    private BlockEntityElectricFence fenceEntity;

    /**
     * Only break a fence once and a while.
     */
    private boolean hasBrokenFence;

    public EntityAttackFenceAI(CreatureEntity entity) {
        this.entity = entity;
        this.hasBrokenFence = false;
    }

    @Override
    public void start() {
        PathNavigator navigator = entity.getNavigation();
        navigator.moveTo(navigator.createPath(fence, 0), entity.getSpeed() + 0.1F);
    }

    @Override
    public void tick() {
        Vector3d position = entity.position();
        List<BlockPos> pos = AIUtils.traverseXZ((int) position.x, (int) position.y, (int) position.z, 1);
        World world = entity.level;
        for (BlockPos bPos : pos) {
            if (world.getBlockState(bPos).getBlock() instanceof BlockElectricFence) {
                fenceEntity.breakFence(6);
                hasBrokenFence = true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !hasBrokenFence && this.canUse();
    }

    @Override
    public boolean canUse() {
        if (this.entity.getRandom().nextFloat() <= 0.5F && !hasBrokenFence) {
            Vector3d position = entity.position();
            List<BlockPos> pos = AIUtils.traverseXZ((int) position.x, (int) position.y, (int) position.z, 10);
            for (BlockPos bPos : pos) {
                if (entity.level.getBlockState(bPos).getBlock() instanceof BlockElectricFence) {
                    TileEntity te = entity.level.getBlockEntity(bPos);
                    if (te instanceof BlockEntityElectricFence) {
                        fenceEntity = (BlockEntityElectricFence) te;
                        for (Connection connection : fenceEntity.getConnections()) {
                            if (!connection.isPowered(entity.level)) {
                                this.fence = bPos;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
