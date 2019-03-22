package net.dumbcode.projectnublar.server.entity.system.ai;

import lombok.ToString;
import net.dumbcode.dumblibrary.server.ai.AIType;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.EntityProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.utils.BlockStateWorker;
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

public class FeedingAi extends EntityAIBase {
    private final EntityLiving entityLiving;
    private final MetabolismComponent metabolism;
    private final BlockStateWorker worker;

    private FeedingProcess process = null;

    public FeedingAi(EntityLiving entityLiving, MetabolismComponent metabolism) {
        this.entityLiving = entityLiving;
        this.metabolism = metabolism;
        this.worker = new BlockStateWorker((state, pos) -> this.metabolism.diet.getBlocks().contains(state) && this.entityLiving.getNavigator().getPathToPos(pos) != null, entityLiving, metabolism.foodSmellDistance);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.metabolism.food <= 10 || true) {
            if(this.process == null) {
                World world = this.entityLiving.world;
                //Search entities first
                for (Entity entity : world.loadedEntityList) {
                    if (entity.getDistanceSq(this.entityLiving) < this.metabolism.foodSmellDistance*this.metabolism.foodSmellDistance) {
                        if (entity instanceof EntityItem && this.metabolism.diet.test(((EntityItem) entity).getItem())) {
                            this.process = new ItemStackProcess((EntityItem) entity);
                            break;
                        } else if (this.metabolism.diet.test(entity)) {
                            this.process = new EntityProcess(entity);
                            break;
                        }
                    }
                }

                if (this.process == null) {
                    if (!this.worker.isActivated()) {
                        this.worker.activate();
                    }
                    List<BlockPos> results = this.worker.getResults();
                    Vec3d pos = this.entityLiving.getPositionVector();
                    if (!results.isEmpty()) {
                        results.sort(Comparator.comparingDouble(o -> o.distanceSq(pos.x, pos.y, pos.z)));
                        this.process = new BlockStateProcess(world, results.get(0));
                    }
                }
                if(this.process != null) {
                    System.out.println(this.process.toString());
                }
            }
            if (this.process != null) {
                return this.process.active();
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.process != null && this.process.active();
    }

    @Override
    public void resetTask() {
        this.process = null;
    }

    public interface FeedingProcess {
       boolean active();
       Vec3d position();
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
    }
}
