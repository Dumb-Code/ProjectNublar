package net.dumbcode.projectnublar.server.entity.ai;

import lombok.ToString;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.BlockStateWorker;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FeedingAI extends EntityAIBase {

    private final ComponentAccess access;
    private final EntityLiving entityLiving;
    private final MetabolismComponent metabolism;
    private Future<List<BlockPos>> blockPosList;

    private FeedingProcess process = null;
    private int eatingTicks;

    public  FeedingAI(ComponentAccess access, EntityLiving entityLiving, MetabolismComponent metabolism) {
        this.access = access;
        this.entityLiving = entityLiving;
        this.metabolism = metabolism;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.metabolism.getFood() <= 3600) {
            if (this.process == null) {
                World world = this.entityLiving.world;
                //Search entities first
                for (Entity entity : world.loadedEntityList) {
                    if (entity.getDistanceSq(this.entityLiving) < this.metabolism.getFoodSmellDistance() * this.metabolism.getFoodSmellDistance()) {
                        if (entity instanceof EntityItem && this.metabolism.getDiet().getResult(((EntityItem) entity).getItem()).isPresent()) {
                            this.process = new ItemStackProcess((EntityItem) entity);
                            break;
                        } else if (this.metabolism.getDiet().getResult(entity).isPresent()) {
                            this.process = new EntityProcess(entity);
                            break;
                        }
                    }
                }
                if(this.process == null) {
                    if(this.blockPosList == null) {
                        this.blockPosList = BlockStateWorker.INSTANCE.runTask(entityLiving.world, entityLiving.getPosition(), metabolism.getFoodSmellDistance(), (w, pos) -> this.metabolism.getDiet().getResult(w.getBlockState(pos)).isPresent() && this.entityLiving.getNavigator().getPathToPos(pos) != null);
                    } else if(this.blockPosList.isDone()) {
                        try {
                            List<BlockPos> results = this.blockPosList.get();
                            this.blockPosList = null;
                            Vec3d pos = this.entityLiving.getPositionVector();
                            if (!results.isEmpty()) {
                                results.sort(Comparator.comparingDouble(o -> o.distanceSq(pos.x, pos.y, pos.z)));
                                for (BlockPos result : results) {
                                    if(this.metabolism.getDiet().getResult(this.entityLiving.world.getBlockState(result)).isPresent()) {
                                        this.process = new BlockStateProcess(world, result);
                                        break;
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            DumbLibrary.getLogger().warn("Unable to finish process, had to interrupt", e);
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            DumbLibrary.getLogger().warn("Unable to finish process", e);
                        }
                    }
                }

            }
            if (this.process != null) {
                return this.process.active();
            }
        }
        return false;
    }

    @Override
    public void updateTask() {
        if(this.process != null) {
            Vec3d position = this.process.position();
            if(this.entityLiving.getPositionVector().squareDistanceTo(position) <= 2*2) {
                this.entityLiving.getNavigator().setPath(null, 0F);
                this.entityLiving.getLookHelper().setLookPosition(position.x, position.y, position.z, this.entityLiving.getHorizontalFaceSpeed(), this.entityLiving.getVerticalFaceSpeed());
                if(this.eatingTicks == 0) {
                    this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a ->
                        a.playAnimation(this.access, new AnimationLayer.AnimationEntry(AnimationHandler.EATING), MetabolismComponent.METABOLISM_CHANNEL)
                    );
                }
                if(this.eatingTicks++ >= this.metabolism.getFoodTicks()) {
                    FeedingResult result = this.process.consume();
                    this.metabolism.setFood(this.metabolism.getFood() + result.getFood());
                    this.metabolism.setWater(this.metabolism.getWater() + result.getWater());
                    this.eatingTicks = 0;
                }
            } else {
                this.entityLiving.getNavigator().tryMoveToXYZ(position.x, position.y, position.z, 0.4D);
            }
        }
        super.updateTask();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.process != null && this.process.active() && this.metabolism.getFood() < (this.metabolism.getMaxFood() / 4) * 3;
    }

    @Override
    public void resetTask() {
        this.process = null;
        this.eatingTicks = 0;
        this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> a.stopAnimation(MetabolismComponent.METABOLISM_CHANNEL));
    }


    public interface FeedingProcess {
        boolean active();

        Vec3d position();

        FeedingResult consume();
    }

    @ToString
    public class ItemStackProcess implements FeedingProcess {

        private final EntityItem entity;

        public ItemStackProcess(EntityItem entity) {
            this.entity = entity;
        }

        @Override
        public boolean active() {
            return !this.entity.isDead && !this.entity.getItem().isEmpty();
        }

        @Override
        public Vec3d position() {
            return this.entity.getPositionVector();
        }

        @Override
        public FeedingResult consume() {
            FeedingResult result = metabolism.getDiet().getResult(this.entity.getItem()).orElse(new FeedingResult(0, 0));
            this.entity.getItem().shrink(1);
            this.entity.setItem(this.entity.getItem());
            return result;
        }
    }

    @ToString
    public class EntityProcess implements FeedingProcess {

        private final Entity entity;

        public EntityProcess(Entity entity) {
            this.entity = entity;
        }

        @Override
        public boolean active() {
            return !this.entity.isDead;
        }

        @Override
        public Vec3d position() {
            return this.entity.getPositionVector();
        }

        @Override
        public FeedingResult consume() {
            this.entity.setDead();
            return metabolism.getDiet().getResult(this.entity).orElse(new FeedingResult(0, 0));
        }
    }

    @ToString
    public class BlockStateProcess implements FeedingProcess {

        private final World world;
        private final BlockPos position;
        private final IBlockState initialState;

        public BlockStateProcess(World world, BlockPos position) {
            this.world = world;
            this.position = position;
            this.initialState = this.world.getBlockState(this.position);
        }

        @Override
        public boolean active() {
            return this.world.getBlockState(this.position) == this.initialState;
        }

        @Override
        public Vec3d position() {
            return new Vec3d(this.position);
        }

        @Override
        public FeedingResult consume() {
            this.world.setBlockToAir(this.position);
            return metabolism.getDiet().getResult(this.initialState).orElse(new FeedingResult(0, 0));
        }
    }
}