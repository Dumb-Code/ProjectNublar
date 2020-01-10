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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DrinkingAI extends EntityAIBase {

    private final ComponentAccess access;
    private EntityLiving entity;
    private Future<List<BlockPos>> blockPosList;
    private BlockPos foundPos;
    private MetabolismComponent metabolism;
    private int drinkingTicks;

    private static final int WATER_THRESHOLD = 3600; // TODO: Vary

    public DrinkingAI(ComponentAccess access, EntityLiving entity, MetabolismComponent metabolism) {
        this.access = access;
        this.entity = entity;
        this.metabolism = metabolism;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if(this.metabolism.water <= WATER_THRESHOLD || true) {
            if(this.blockPosList == null) {
                this.blockPosList = BlockStateWorker.INSTANCE.runTask(this.entity.world, this.entity.getPosition(), 25, (state, pos) -> state.getMaterial() == Material.WATER);
            }
            if(this.blockPosList.isDone()) {
                try {
                    List<BlockPos> blockPos = this.blockPosList.get();
                    blockPos.sort(Comparator.comparingDouble(o -> o.distanceSq(this.entity.posX, this.entity.posY, this.entity.posZ)));
                    for (BlockPos pos : blockPos) {
                        if(this.entity.world.getBlockState(pos).getMaterial() == Material.WATER) {
                            this.foundPos = pos;
                            return true;
                        }
                    }
                }  catch (InterruptedException e) {
                    DumbLibrary.getLogger().warn("Unable to finish process, had to interrupt", e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    DumbLibrary.getLogger().warn("Unable to finish process", e);
                }
                this.blockPosList = null;
            }
        }
        return false;
    }

    @Override
    public void updateTask() {
        Vec3d position = new Vec3d(this.foundPos.getX() + 0.5D, this.foundPos.getY() + 0.5D, this.foundPos.getZ() + 0.5D);
        if(this.entity.getPositionVector().squareDistanceTo(position) <= 2*2) {
            this.entity.getNavigator().setPath(null, 0F);
            this.entity.getLookHelper().setLookPosition(position.x, position.y, position.z, this.entity.getHorizontalFaceSpeed(), this.entity.getVerticalFaceSpeed());
            if(this.drinkingTicks == 0) {
                this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a ->
                    a.playAnimation(this.access, new AnimationLayer.AnimationEntry(AnimationHandler.DRINKING), MetabolismComponent.METABOLISM_CHANNEL)
                );
            }
            if(this.drinkingTicks++ >= this.metabolism.waterTicks) {
                this.drinkingTicks = 0;
            } else {
                this.metabolism.water += this.metabolism.hydrateAmountPerTick;
            }
        } else {
            this.entity.getNavigator().tryMoveToXYZ(position.x, position.y, position.z, 0.4D);
        }
        this.entity.getNavigator().tryMoveToXYZ(this.foundPos.getX(), this.foundPos.getY(), this.foundPos.getZ(), this.entity.getAIMoveSpeed());

        super.updateTask();
    }

    @Override
    public void resetTask() {
        this.drinkingTicks = 0;
        this.foundPos = null;
        this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> a.stopAnimation(MetabolismComponent.METABOLISM_CHANNEL));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.entity.world.getBlockState(this.foundPos).getMaterial() == Material.WATER && this.metabolism.water < (this.metabolism.maxWater / 4) * 3;
    }
}