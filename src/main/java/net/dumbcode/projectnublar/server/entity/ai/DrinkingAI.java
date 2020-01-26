package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.BlockStateWorker;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DrinkingAI extends EntityAIBase {

    private final ComponentAccess access;
    private EntityLiving entity;
    private Future<List<BlockPos>> blockPosList;
    private final List<BlockPos> foundPositions = new ArrayList<>();
    private MetabolismComponent metabolism;
    private int drinkingTicks;

    private Path path;

    private static final int WATER_THRESHOLD = 3600; // TODO: Vary

    public DrinkingAI(ComponentAccess access, EntityLiving entity, MetabolismComponent metabolism) {
        this.access = access;
        this.entity = entity;
        this.metabolism = metabolism;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if(this.metabolism.getWater() <= WATER_THRESHOLD) {
            if(this.blockPosList == null) {
                this.blockPosList = BlockStateWorker.INSTANCE.runTask(this.entity.world, this.entity.getPosition(), 50, 50, 7, (world, pos) -> world.getBlockState(pos).getMaterial() == Material.WATER && world.isAirBlock(pos.up()));
            }
            if(this.blockPosList.isDone()) {
                try {
                    List<BlockPos> blockPos = this.blockPosList.get();
                    blockPos.sort(Comparator.comparingDouble(o -> o.distanceSq(this.entity.posX, this.entity.posY, this.entity.posZ)));
                    for (BlockPos pos : blockPos) {
                        if(this.entity.world.getBlockState(pos).getMaterial() == Material.WATER) {
                            this.foundPositions.add(pos);
                        }
                    }
                }  catch (InterruptedException e) {
                    DumbLibrary.getLogger().warn("Unable to finish process, had to interrupt", e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DumbLibrary.getLogger().warn("Unable to finish process", e);
                } finally {
                    this.blockPosList = null;
                }
                return !this.foundPositions.isEmpty();
            }
        }
        return false;
    }


    @Override
    public void updateTask() {
        //This is to make sure that our destination is reachable:
        if(this.path == null) {
            boolean foundPath = false;
            for (int i = 0; i < 15; i++) {
                BlockPos foundPos = this.foundPositions.get(0);
                this.path = this.entity.getNavigator().getPathToPos(foundPos);
                if(this.path != null) {
                    PathPoint finalPoint = this.path.getFinalPathPoint();
                    if(finalPoint != null && foundPos.add(-finalPoint.x, -finalPoint.y, -finalPoint.z).distanceSq(Vec3i.NULL_VECTOR) < 2*2) {
                        foundPath = true;
                        break;
                    } else {
                        this.foundPositions.remove(0);
                    }
                }
            }
            if(!foundPath) {
                this.path = null;
                return;
            }
        }

        BlockPos foundPos = this.foundPositions.get(0);
        Vec3d position = new Vec3d(foundPos.getX() + 0.5D, foundPos.getY() + 0.5D, foundPos.getZ() + 0.5D);
        if(this.entity.getPositionVector().squareDistanceTo(position) <= 2*2) {
            this.entity.getNavigator().setPath(null, 0F);
            this.entity.getLookHelper().setLookPosition(position.x, position.y, position.z, this.entity.getHorizontalFaceSpeed(), this.entity.getVerticalFaceSpeed());
            if(this.drinkingTicks == 0) {
                this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a ->
                    a.playAnimation(this.access, new AnimationLayer.AnimationEntry(AnimationHandler.DRINKING), MetabolismComponent.METABOLISM_CHANNEL)
                );
            }
            if(this.drinkingTicks++ >= this.metabolism.getWaterTicks()) {
                this.drinkingTicks = 0;
            } else {
                this.metabolism.setWater(this.metabolism.getWater() + this.metabolism.getHydrateAmountPerTick());
            }
        } else {
            this.entity.getNavigator().setPath(this.path, 0.5F);
        }

        super.updateTask();
    }

    @Override
    public void resetTask() {
        this.drinkingTicks = 0;
        this.foundPositions.clear();
        this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> a.stopAnimation(MetabolismComponent.METABOLISM_CHANNEL));
    }

    @Override
    public boolean shouldContinueExecuting() {
        if(this.foundPositions.isEmpty() || this.path == null) {
            return false;
        }
        BlockPos foundPos = this.foundPositions.get(0);
        Vec3d position = new Vec3d(foundPos.getX() + 0.5D, foundPos.getY() + 0.5D, foundPos.getZ() + 0.5D);
        return (this.entity.getNavigator().getPath() == this.path || this.entity.getPositionVector().squareDistanceTo(position) <= 2*2) && this.entity.world.getBlockState(foundPos).getMaterial() == Material.WATER && this.metabolism.getWater() < (this.metabolism.getMaxWater() / 4) * 3;
    }
}