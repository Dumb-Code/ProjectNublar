package net.dumbcode.projectnublar.server.world.structures;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.PredicateTraverser;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.StructurePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Getter
public abstract class StructureInstance {

    protected final World world;
    protected final BlockPos position;

    protected final int children;

    protected final int xSize;
    protected final int zSize;

    protected final List<StructurePredicate> predicates;

    protected final List<String> globalPredicates = new ArrayList<>();

    public StructureInstance(World world, BlockPos position, int xSize, int zSize, Structure structure, StructurePredicate... predicates) {
        this.world = world;
        this.children = structure.getChildren();
        BlockPos mut = new BlockPos(position.getX(), 257, position.getZ());
        while (mut.getY() > 0 && !world.isSideSolid(mut.down(), EnumFacing.UP)) {
            mut = mut.down();
        }
        this.position = mut;
        this.xSize = xSize;
        this.zSize = zSize;
        this.predicates = Lists.newArrayList(predicates);
        this.globalPredicates.addAll(structure.globalPredicates);
    }

    private boolean applyPredicates() {
        for (StructurePredicate predicate : this.predicates) {
            if(!predicate.canBuildDirect(this)) {
                return false;
            }
        }

        List<PredicateTraverser> traversers = new ArrayList<>();
        for (StructurePredicate predicate : this.predicates) {
            predicate.setupTraversers(traversers::add);
        }
        this.traverseTopdown(pos -> traversers.forEach(t -> t.onTraverse(this, pos)));

        for (PredicateTraverser traverser : traversers) {
            if(!traverser.acceptable()) {
                return false;
            }
        }
        return true;
    }

    public Rectangle createPredicateBounds() {
        return new Rectangle(0, 0, this.xSize, this.zSize);
    }


    public boolean canBuild() {
        return this.applyPredicates();
    }

    public abstract void build(Random random, List<DataHandler> handlers);

    protected void traverseTopdown(Consumer<BlockPos> consumer) {
        for (int x = 0; x <= this.xSize; x++) {
            for (int z = 0; z <= this.zSize; z++) {
                BlockPos pos = this.position.add(x, 0, z);

                Chunk chunk = this.world.getChunk(pos);
                BlockPos blockpos;
                BlockPos blockpos1;
                for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
                    blockpos1 = blockpos.down();
                    IBlockState state = chunk.getBlockState(blockpos1);

                    if (
                        (state.getMaterial().isLiquid() || state.getMaterial().blocksMovement()) &&
                            !state.getBlock().isLeaves(state, this.world, blockpos1) &&
                            !state.getBlock().isFoliage(this.world, blockpos1) &&
                            !state.getBlock().isReplaceable(this.world, blockpos1)
                    ) {
                        break;
                    }
                }
                consumer.accept(blockpos);
            }
        }

    }
}
