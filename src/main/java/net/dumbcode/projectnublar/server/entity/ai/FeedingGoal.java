package net.dumbcode.projectnublar.server.entity.ai;

import lombok.ToString;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.CullSizeComponent;
import net.dumbcode.dumblibrary.server.utils.BlockStateWorker;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

// Things to do:
//  + for EntityFeeding (read: hunting)
//  + add a high energy requirement to start feeding
//  + modify energy rate to be negative
//
// When a feeding goal cannot be found (no food), look in the herd saved data food locations, and try again
public class FeedingGoal extends EntityGoal {

    private final ComponentAccess access;
    private final MobEntity entityLiving;
    private final MetabolismComponent metabolism;
    private Future<List<BlockPos>> blockPosList;

    private FeedingProcess process = null;
    private int eatingTicks;

    public FeedingGoal(GoalManager manager, ComponentAccess access, MobEntity entityLiving, MetabolismComponent metabolism) {
        super(manager);
        this.access = access;
        this.entityLiving = entityLiving;
        this.metabolism = metabolism;
    }


    private void findNewProcess() {
        World world = this.entityLiving.level;
        //Search entities first
        Entity targetEntity = null;
        boolean isTargetItem = false;
        for (Entity entity : world.getEntities(this.entityLiving, this.entityLiving.getBoundingBox().inflate(this.metabolism.getFoodSmellDistance()), EntityPredicates.NO_CREATIVE_OR_SPECTATOR)) {
            double dist = entity.distanceTo(this.entityLiving);

            boolean isEntityItem = entity instanceof ItemEntity && this.metabolism.getDiet().getResult(((ItemEntity) entity).getItem()).isPresent();
            boolean isNormalEntity = this.metabolism.getDiet().getResult(entity).isPresent();

            boolean closer = targetEntity == null || targetEntity.distanceTo(this.entityLiving) < dist;

            //If we've found not an item and we're targeting an item
            if(isTargetItem && !isEntityItem) {
                continue;
            }

            //If the target entity isn't an item and we've found an item:
            if(isEntityItem && (!isTargetItem || closer)) {
                this.process = new ItemStackProcess((ItemEntity) entity);
                targetEntity = entity;
                isTargetItem = true;
                continue;
            }

            if(closer && isNormalEntity) {
//                this.process = new EntityProcess(entity);
                targetEntity = entity;
                isTargetItem = false;
            }
        }
        if(this.process == null) {
            if(this.blockPosList == null) {
                this.blockPosList = BlockStateWorker.INSTANCE.runTask(entityLiving.level, entityLiving.blockPosition(), metabolism.getFoodSmellDistance(),
                    (w, pos) -> this.metabolism.getDiet().getResult(w.getBlockState(pos)).isPresent() && this.entityLiving.getNavigation().getPath() != null
                );
            } else if(this.blockPosList.isDone()) {
                try {
                    List<BlockPos> results = this.blockPosList.get();
                    this.blockPosList = null;
                    Vector3d pos = this.entityLiving.position();
                    if (!results.isEmpty()) {
                        results.sort(Comparator.comparingDouble(o -> o.distSqr(pos.x, pos.y, pos.z, true)));
                        for (BlockPos result : results) {
                            if(this.metabolism.getDiet().getResult(this.entityLiving.level.getBlockState(result)).isPresent()) {
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


    @Override
    protected OptionalDouble getImportance() {
        if (this.metabolism.getFood() <= 2500) {
            return OptionalDouble.of(2500 / this.metabolism.getFood());
        }
        return OptionalDouble.empty();
    }

    @Override
    public boolean onStarted() {
        if (this.process == null || !this.process.active()) {
            this.findNewProcess();
        }
        if (this.process != null) {
            return this.process.active();
        }
        return false;
    }

    @Override
    public void tick() {
        if(this.process != null) {
            if(this.process.active()) {
                Vector3d position = this.process.position();
                double cullSizeHalf = Math.max(this.entityLiving.getBoundingBoxForCulling().getXsize(), this.entityLiving.getBoundingBoxForCulling().getZsize()) / 4F;
                if(this.entityLiving.position().distanceToSqr(position) <= cullSizeHalf*cullSizeHalf) {
                    this.entityLiving.getNavigation().stop();
                    this.entityLiving.getLookControl().setLookAt(position.x, position.y, position.z, this.entityLiving.getMaxHeadYRot(), this.entityLiving.getMaxHeadXRot());
                    this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
                        Animation animation = this.process.getAnimation();
                        if(!a.isAnimationPlaying(animation, MetabolismComponent.METABOLISM_CHANNEL)) {
                            FeedingProcess currentProcess = this.process;
                            a.playAnimation(animation, MetabolismComponent.METABOLISM_CHANNEL).loopUntil(() -> currentProcess == null || !currentProcess.active());
                        }
                    });

                    if(this.eatingTicks == 11) {
                        this.process.onProcessRan();
                    }
                    //todo: move to animation event?
                    if(this.eatingTicks++ >= this.metabolism.getFoodTicks()) {
                        FeedingResult result = this.process.consume();
                        this.metabolism.setFood(this.metabolism.getFood() + result.getFood());
                        this.metabolism.setWater(this.metabolism.getWater() + result.getWater());
                        this.eatingTicks = 0;
                    }
                } else {
                    this.entityLiving.getNavigation().moveTo(position.x, position.y, position.z, 0.65D);
                }
            }


            if(!this.process.active()) {
                this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
                    Animation animation = this.process.getAnimation();
                    if(!a.isAnimationPlaying(animation, MetabolismComponent.METABOLISM_CHANNEL)) {
                        this.finish();
                    }
                });
            }
        }
    }

    @Override
    public void onFinished() {
        this.process = null;
        this.eatingTicks = 0;
        this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> a.stopAnimation(MetabolismComponent.METABOLISM_CHANNEL));
    }

    public interface FeedingProcess {
        boolean active();

        default void onProcessRan() {};

        default Animation getAnimation() {
            return AnimationHandler.EATING;
        }

        Vector3d position();

        FeedingResult consume();
    }

    @ToString
    public class ItemStackProcess implements FeedingProcess {

        private final ItemEntity entity;

        public ItemStackProcess(ItemEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean active() {
            return this.entity.isAlive() && !this.entity.getItem().isEmpty();
        }

        @Override
        public Vector3d position() {
            return this.entity.position();
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
            return this.entity.isAlive();
        }

        @Override
        public Vector3d position() {
            return this.entity.position();
        }

        @Override
        public void onProcessRan() {
            if(this.entity instanceof LivingEntity) {
                this.entity.hurt(DamageSource.mobAttack(entityLiving), 6F);
            }
        }

        @Override
        public Animation getAnimation() {
            return AnimationHandler.ATTACK;
        }

        @Override
        public FeedingResult consume() {
            this.entity.kill();
            return metabolism.getDiet().getResult(this.entity).orElse(new FeedingResult(0, 0));
        }
    }

    @ToString
    public class BlockStateProcess implements FeedingProcess {

        private final World world;
        private final BlockPos position;
        private final BlockState initialState;

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
        public Vector3d position() {
            return new Vector3d(this.position.getX(), this.position.getY(), this.position.getZ());
        }

        @Override
        public FeedingResult consume() {
            this.world.setBlock(this.position, Blocks.AIR.defaultBlockState(), 3);
            return metabolism.getDiet().getResult(this.initialState).orElse(new FeedingResult(0, 0));
        }
    }
}