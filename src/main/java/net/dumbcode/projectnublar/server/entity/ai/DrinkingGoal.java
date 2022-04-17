package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.BlockStateWorker;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DrinkingGoal extends EntityGoal {

    private final ComponentAccess access;
    private final MobEntity entity;
    private Future<List<BlockPos>> blockPosList;
    private final List<BlockPos> foundPositions = new ArrayList<>();
    private final MetabolismComponent metabolism;
    private int drinkingTicks;

    private Path path;

    private static final int WATER_THRESHOLD = 3500; // TODO: Vary

    public DrinkingGoal(GoalManager manager, ComponentAccess access, MobEntity entity, MetabolismComponent metabolism) {
        super(manager);
        this.access = access;
        this.entity = entity;
        this.metabolism = metabolism;
    }


    @Override
    protected OptionalDouble getImportance() {
        if (this.metabolism.getFood() <= WATER_THRESHOLD) {
            return OptionalDouble.of(WATER_THRESHOLD / this.metabolism.getWater());
        }
        return OptionalDouble.empty();
    }

    @Override
    public boolean onStarted() {
        if(this.blockPosList == null) {
            this.blockPosList = BlockStateWorker.INSTANCE.runTask(this.entity.level, this.entity.blockPosition(), 50, 7, 50, (world, pos) -> world.getBlockState(pos).getMaterial() == Material.WATER && world.getBlockState(pos.above()).isAir(world, pos.above()));
        }
        if(this.blockPosList.isDone()) {
            try {
                List<BlockPos> blockPos = this.blockPosList.get();
                blockPos.sort(Comparator.comparingDouble(o -> this.entity.position().distanceToSqr(o.getX(), o.getY(), o.getZ())));
                for (BlockPos pos : blockPos) {
                    if(this.entity.level.getBlockState(pos).getMaterial() == Material.WATER) {
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
        return false;
    }


    @Override
    public void tick() {
        //This is to make sure that our destination is reachable:
        if(this.path == null) {
            boolean foundPath = false;
            for (int i = 0; i < 15; i++) {
                if(this.foundPositions.isEmpty()) {
                    this.finish();
                    return;
                }
                BlockPos foundPos = this.foundPositions.get(0).above();
                this.path = this.entity.getNavigation().createPath(foundPos, 1);
                if(this.path != null) {
                    PathPoint finalPoint = this.path.getEndNode();
                    if(finalPoint != null && foundPos.offset(-finalPoint.x, -finalPoint.y, -finalPoint.z).distSqr(Vector3i.ZERO) < 2*2) {
                        foundPath = true;
                        break;
                    } else {
                        this.foundPositions.remove(0);
                    }
                }
            }
            if(!foundPath) {
                this.path = null;
                this.finish();
                return;
            }
        }

        BlockPos foundPos = this.foundPositions.get(0);
        Vector3d position = new Vector3d(foundPos.getX() + 0.5D, foundPos.getY() + 0.5D, foundPos.getZ() + 0.5D);
        double cullSizeHalf = Math.max(this.entity.getBoundingBoxForCulling().getXsize(), this.entity.getBoundingBoxForCulling().getZsize()) / 4F;

        if(this.entity.position().distanceToSqr(position) <= cullSizeHalf*cullSizeHalf) {
            this.entity.getNavigation().stop();
            this.entity.getLookControl().setLookAt(position.x, position.y, position.z, this.entity.getMaxHeadYRot(), this.entity.getMaxHeadXRot());
            if(this.drinkingTicks == 0) {
                this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
                    if(!a.isAnimationPlaying(AnimationHandler.DRINKING, MetabolismComponent.METABOLISM_CHANNEL)) {
                        a.playAnimation(AnimationHandler.DRINKING, MetabolismComponent.METABOLISM_CHANNEL).loopUntil(() -> this.drinkingTicks < this.metabolism.getWaterTicks());
                    }
                });
            }
            this.drinkingTicks++;
            this.metabolism.setWater(this.metabolism.getWater() + this.metabolism.getHydrateAmountPerTick());

        } else {
            this.entity.getNavigation().moveTo(this.path, 0.7F);
        }

        if(this.drinkingTicks >= this.metabolism.getWaterTicks()) {
            this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
                if(!a.isAnimationPlaying(AnimationHandler.DRINKING, MetabolismComponent.METABOLISM_CHANNEL)) {
                    this.finish();
                }
            });
            this.finish();
        }
    }

    @Override
    public void onFinished() {
        this.drinkingTicks = 0;
        this.foundPositions.clear();
    }
}