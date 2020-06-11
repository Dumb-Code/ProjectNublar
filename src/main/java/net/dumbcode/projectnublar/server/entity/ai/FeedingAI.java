package net.dumbcode.projectnublar.server.entity.ai;

import lombok.ToString;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSound;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.utils.BlockStateWorker;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
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

    public FeedingAI(ComponentAccess access, EntityLiving entityLiving, MetabolismComponent metabolism) {
        this.access = access;
        this.entityLiving = entityLiving;
        this.metabolism = metabolism;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.metabolism.getFood() <= 7500) {
            if (this.process == null) {
                World world = this.entityLiving.world;
                //Search entities first
                Entity targetEntity = null;
                boolean isTargetItem = false;
                for (Entity entity : world.loadedEntityList) {
                    double dist = entity.getDistanceSq(this.entityLiving);
                    if (dist < this.metabolism.getFoodSmellDistance() * this.metabolism.getFoodSmellDistance()) {
                        boolean isEntityItem = entity instanceof EntityItem && this.metabolism.getDiet().getResult(((EntityItem) entity).getItem()).isPresent();
                        boolean isNormalEntity = this.metabolism.getDiet().getResult(entity).isPresent();

                        boolean closer = targetEntity == null || targetEntity.getDistanceSq(this.entityLiving) < dist;

                        //If we've found not an item and we're targeting an item
                        if(isTargetItem && !isEntityItem) {
                            continue;
                        }

                        //If the target entity isn't an item and we've found an item:
                        if(isEntityItem && (!isTargetItem || closer)) {
                            this.process = new ItemStackProcess((EntityItem) entity);
                            targetEntity = entity;
                            isTargetItem = true;
                            continue;
                        }

                        if(closer && isNormalEntity) {
                            this.process = new EntityProcess(entity);
                            targetEntity = entity;
                            isTargetItem = false;
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
                this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
                    Animation animation = this.process.getAnimation();
                    AnimationWrap wrap = a.getWrap(MetabolismComponent.METABOLISM_CHANNEL);
                    if(wrap ==  null || wrap.isInvalidated() || (wrap.getEntry().getAnimation() != animation)) {
                        a.playAnimation(this.access, new AnimationEntry(animation), MetabolismComponent.METABOLISM_CHANNEL);
                    }
                });

                //TODO-stream: don't have this directly. THIS IS JUST FOR THE VELOCIRAPTOR
                //What I should do is have an animation, then also have an "audio timeline" that plays spefic events at times.
                if (!(this.process instanceof EntityProcess)) {
                    if(this.eatingTicks == 41 || this.eatingTicks == 77) {
                        this.access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.EATING_RIP).ifPresent(e ->
                            this.entityLiving.world.playSound(null, this.entityLiving.posX, this.entityLiving.posY, this.entityLiving.posZ, e, SoundCategory.AMBIENT, this.entityLiving.getRNG().nextFloat()*0.25F+0.2F, this.entityLiving.getRNG().nextFloat()*0.5F+0.75F)
                        );
                    }
                    if(this.eatingTicks == 60 || this.eatingTicks == 95) {
                        this.access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.EATING_CRUNCH).ifPresent(e ->
                            this.entityLiving.world.playSound(null, this.entityLiving.posX, this.entityLiving.posY, this.entityLiving.posZ, e, SoundCategory.AMBIENT, this.entityLiving.getRNG().nextFloat()*0.25F+0.2F, this.entityLiving.getRNG().nextFloat()*0.5F+0.75F)
                        );
                    }
                    this.process.tick();
                } else {
                    if(this.eatingTicks == 11) {
                        this.access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.ATTACKING).ifPresent(e ->
                            this.entityLiving.world.playSound(null, this.entityLiving.posX, this.entityLiving.posY, this.entityLiving.posZ, e, SoundCategory.AMBIENT, this.entityLiving.getRNG().nextFloat()*0.25F+0.2F, this.entityLiving.getRNG().nextFloat()*0.5F+0.75F)
                        );
                        this.process.tick();
                    }
                }
                if(this.eatingTicks++ >= this.metabolism.getFoodTicks()) {
                    FeedingResult result = this.process.consume();
                    this.metabolism.setFood(this.metabolism.getFood() + result.getFood());
                    this.metabolism.setWater(this.metabolism.getWater() + result.getWater());
                    this.eatingTicks = 0;
                }
            } else {
                this.entityLiving.getNavigator().tryMoveToXYZ(position.x, position.y, position.z, 0.65D);
            }
        }
        super.updateTask();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.process != null && this.process.active() && this.eatingTicks < this.metabolism.getFoodTicks();
    }

    @Override
    public void resetTask() {
        this.process = null;
        this.eatingTicks = 0;
        this.access.get(EntityComponentTypes.ANIMATION).ifPresent(a -> a.stopAnimation(this.entityLiving, MetabolismComponent.METABOLISM_CHANNEL));
    }


    public interface FeedingProcess {
        boolean active();

        default void tick() {};

        default Animation getAnimation() {
            return AnimationHandler.EATING;
        }

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
        public void tick() {
            if(this.entity instanceof EntityLivingBase) {
                this.entity.attackEntityFrom(DamageSource.causeMobDamage(entityLiving), 6F);
            }
        }

        @Override
        public Animation getAnimation() {
            return AnimationHandler.ATTACK;
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