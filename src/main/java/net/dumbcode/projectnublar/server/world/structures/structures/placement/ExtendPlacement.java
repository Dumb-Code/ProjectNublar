package net.dumbcode.projectnublar.server.world.structures.structures.placement;

import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;

public class ExtendPlacement implements StructurePlacement {

    private final int yStart;

    public ExtendPlacement(int yStart) {
        this.yStart = yStart;
    }

    @Override
    public BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition) {
        BlockPos start = worldPosition.subtract(relativePosition);
        int total = 0;
        for (int x = 0; x <= instance.getXSize(); x++) {
            for (int z = 0; z <= instance.getZSize(); z++) {
                total += WorldUtils.getDirectTopdownBlock(world, start.offset(x, 0, z)).getY() - 1;
            }
        }
        return new BlockPos(worldPosition.getX(), total / ((instance.getXSize() + 1) * (instance.getZSize() + 1)), worldPosition.getZ()).above(relativePosition.getY());
    }

    @Override
    public boolean place(World worldIn, StructureInstance instance, BlockPos pos, BlockPos relativePosition, BlockState state, int flags) {
        if(relativePosition.getY() == 0 && Block.isFaceFull(state.getShape(worldIn, pos), Direction.UP) && Block.isFaceFull(state.getShape(worldIn, pos), Direction.DOWN)) {
            BlockPos mut = pos;
            while (mut.getY() > 0 && !Block.isFaceFull(worldIn.getBlockState(mut.below()).getShape(worldIn, mut.below()), Direction.UP)) {
                worldIn.setBlock(mut, state, flags);
                mut = mut.below();
            }
        }
        return StructurePlacement.super.place(worldIn, instance, pos, relativePosition, state, flags);
    }
}
