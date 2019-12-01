package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.utils.AIUtils;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.entity.component.impl.MoodComponent;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityAttackFenceAI extends EntityAIBase {

    private EntityCreature entity;

    private MoodComponent mood;

    /**
     * Fence that was targeted.
     */
    private BlockPos fence;

    private BlockEntityElectricFence fenceEntity;

    /**
     * Only break a fence once and a while.
     */
    private boolean hasBrokenFence;

    public EntityAttackFenceAI(EntityCreature entity, MoodComponent mood) {
        this.entity = entity;
        this.mood = mood;
        this.hasBrokenFence = false;
    }

    @Override
    public void startExecuting() {
        PathNavigate navigator = entity.getNavigator();
        navigator.setPath(navigator.getPathToPos(fence), entity.getAIMoveSpeed() + 0.1F);
    }

    @Override
    public void updateTask() {
        List<BlockPos> pos = AIUtils.traverseXZ((int) entity.posX, (int) entity.posY, (int) entity.posZ, 1);
        World world = entity.world;
        for (BlockPos bPos : pos) {
            if (world.getBlockState(bPos).getBlock() instanceof BlockElectricFence) {
                fenceEntity.breakFence(6);
                hasBrokenFence = true;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !hasBrokenFence && this.shouldExecute();
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.getRNG().nextFloat() <= 0.5F && mood.getMood() == MoodComponent.MoodType.ANGRY && !hasBrokenFence) {
            List<BlockPos> pos = AIUtils.traverseXZ((int) entity.posX, (int) entity.posY, (int) entity.posZ, 10);
            for (BlockPos bPos : pos) {
                if (entity.world.getBlockState(bPos).getBlock() instanceof BlockElectricFence) {
                    TileEntity te = entity.world.getTileEntity(bPos);
                    if (te instanceof BlockEntityElectricFence) {
                        fenceEntity = (BlockEntityElectricFence) te;
                        for (Connection connection : fenceEntity.getConnections()) {
                            if (!connection.isPowered(entity.world)) {
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
