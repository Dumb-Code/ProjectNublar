package net.dumbcode.projectnublar.server.world.structures.structures.placement;

import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
                total += WorldUtils.getDirectTopdownBlock(world, start.add(x, 0, z)).getY() - 1;
            }
        }
        return new BlockPos(worldPosition.getX(), total / ((instance.getXSize() + 1) * (instance.getZSize() + 1)), worldPosition.getZ()).up(relativePosition.getY());
    }

    @Override
    public boolean place(World worldIn, StructureInstance instance, BlockPos pos, BlockPos relativePosition, IBlockState state, int flags) {
        if(relativePosition.getY() == 0 && state.isSideSolid(worldIn, pos, EnumFacing.UP) && state.isSideSolid(worldIn, pos, EnumFacing.DOWN)) {
            BlockPos mut = pos;
            while (mut.getY() > 0 && !worldIn.isSideSolid(mut.down(), EnumFacing.UP)) {
                worldIn.setBlockState(mut, state, flags);
                mut = mut.down();
            }
        }
        return StructurePlacement.super.place(worldIn, instance, pos, relativePosition, state, flags);
    }
}
