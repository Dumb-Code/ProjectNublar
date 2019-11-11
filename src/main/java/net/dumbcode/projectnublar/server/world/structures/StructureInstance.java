package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

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

    public StructureInstance(World world, BlockPos position, int children, int xSize, int zSize) {
        this.world = world;
        this.children = children;
        BlockPos mut = position.add(-xSize/2, 0, -zSize/2);
        mut = new BlockPos(mut.getX(), 257, mut.getZ());
        while (mut.getY() > 0 && !world.isSideSolid(mut.down(), EnumFacing.UP)) {
            mut = mut.down();
        }
        this.position = mut;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    public boolean canBuild() {
        return true;
    }

    public abstract void build(Random random, List<DataHandler> handlers);

    protected void traverseTopdown(Consumer<BlockPos> consumer) {
        this.traverseTopdown(-this.xSize/2, this.xSize/2, -this.zSize/2, this.zSize/2, consumer);
    }

    protected void traverseTopdown(int minX, int maxX, int minZ, int maxZ, Consumer<BlockPos> consumer) {
        if(minX > maxX) {
            int temp = minX;
            minX = maxX;
            maxX =  temp;
        }

        if(minZ > maxZ) {
            int temp = minZ;
            minZ = maxZ;
            maxZ =  temp;
        }

        for (int x = minX; x < maxX; x++) {
            for (int z = -minZ; z < maxZ; z++) {
                BlockPos pos = this.position.add(x, 0, z);

                Chunk chunk = this.world.getChunk(pos);
                BlockPos blockpos;
                BlockPos blockpos1;
                for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
                    blockpos1 = blockpos.down();
                    IBlockState state = chunk.getBlockState(blockpos1);

                    if (state.getMaterial().isLiquid() || state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, this.world, blockpos1) && !state.getBlock().isFoliage(this.world, blockpos1)) {
                        break;
                    }
                }

                consumer.accept(blockpos);
            }
        }

    }
}
