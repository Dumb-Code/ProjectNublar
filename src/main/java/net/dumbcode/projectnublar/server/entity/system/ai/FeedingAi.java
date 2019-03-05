package net.dumbcode.projectnublar.server.entity.system.ai;

import net.dumbcode.dumblibrary.server.ai.AIType;
import net.dumbcode.dumblibrary.server.ai.AdvancedAIBase;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.EntityProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.utils.BlockStateWorker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class FeedingAi extends AdvancedAIBase {
    private final MetabolismComponent metabolism;
    private final Dinosaur dinosaur;

    private final BlockStateWorker worker;

    public FeedingAi(EntityLiving entity, MetabolismComponent metabolism, Dinosaur dinosaur) {
        super(entity, AIType.METABOLISM);
        this.metabolism = metabolism;
        this.dinosaur = dinosaur;
        this.worker = new BlockStateWorker(this.dinosaur.getEntityProperties().getDiet().getBlocks(), entity, this.dinosaur.getEntityProperties().getDistanceSmellFood());
        this.setUsesCooldown(true);
    }

    @Override
    public boolean shouldContinue() {
        return this.metabolism.food <= 10;
    }

    @Override
    public void checkImportance() {
        this.setImportance(10 - this.metabolism.food);
    }

    @Override
    public void execute() {
        EntityProperties prop = this.dinosaur.getEntityProperties();
        FeedingDiet diet = prop.getDiet();

        World world = this.getEntity().world;

        Entity foundEntity = null;
        ItemStack foundStack = null;
        IBlockState foundState = null;

        //Search entities first
        for (Entity entity : world.loadedEntityList) {
            if(entity.getDistanceSq(this.getEntity()) > prop.getDistanceSmellFood()*prop.getDistanceSmellFood()) {
                if(entity instanceof EntityItem && diet.test(((EntityItem) entity).getItem())) {
                    foundStack = ((EntityItem) entity).getItem();
                    break;
                } else if(diet.test(entity)) {
                    foundEntity = entity;
                    break;
                }
            }
        }

        if(foundEntity == null && foundStack ==  null) {
            if(!this.worker.isActivated()) {
                this.worker.activate();
            }
            List<BlockPos> results = this.worker.getResults();
            Vec3d pos = this.getEntity().getPositionVector();
            if(!results.isEmpty()) {
                results.sort(Comparator.comparingDouble(o -> o.distanceSq(pos.x, pos.y, pos.z)));
                foundState = world.getBlockState(results.get(0));
            }
        }

        super.execute();
    }

    public interface FeedingProcess {
       boolean active();
       void update();
    }

    public class ItemStackProcess {//todo: this shit
//todo: this shit
    }//todo: this shit
//todo: this shit
    public class EntityProcess {//todo: this shit
//todo: this shit
    }//todo: this shit
//todo: this shit
    public class BlockStateProcess {//todo: this shit
//todo: this shit
    }//todo: this shit
}
