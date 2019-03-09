package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BlockStateWorker implements Runnable {

    private final BiPredicate<IBlockState, BlockPos> predicate;
    private final Entity positioned;
    private final int radii;
    private BlockPos previousPosition;
    private boolean activated;


    private final List<BlockPos> results = Lists.newArrayList();

    public BlockStateWorker(BiPredicate<IBlockState, BlockPos> predicate, Entity positioned, int radii) {
        this.predicate = predicate;
        this.positioned = positioned;
        this.radii = radii;
    }

    @Override
    public void run() {
        while(this.results.isEmpty()) {
            if(this.previousPosition == null || this.previousPosition.distanceSq(this.positioned.getPosition()) > 25) {
                for (int x = -this.radii; x < this.radii; x++) {
                    for (int y = -this.radii; y < this.radii; y++) {
                        for (int z = -this.radii; z < this.radii; z++) {
                            BlockPos pos = new BlockPos(x + this.positioned.posX, y + this.positioned.posY, z + this.positioned.posZ);
                            if(this.predicate.test(this.positioned.world.getBlockState(pos), pos)) {
                                this.results.add(pos);
                            }
                        }
                    }
                }
                this.previousPosition = this.positioned.getPosition();
            }
        }
    }

    public void activate() {
        this.activated = true;
        Thread thread = new Thread(this, "");
        thread.setDaemon(true);
    }

    public boolean isActivated() {
        return this.activated;
    }

    public synchronized List<BlockPos> getResults() {
        return this.results;
    }
}
