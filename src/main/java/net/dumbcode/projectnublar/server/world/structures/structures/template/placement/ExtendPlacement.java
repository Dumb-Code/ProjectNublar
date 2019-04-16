package net.dumbcode.projectnublar.server.world.structures.structures.template.placement;

import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExtendPlacement implements TemplatePlacement {

    private final int yStart;

    public ExtendPlacement(int yStart) {
        this.yStart = yStart;
    }

    @Override
    public BlockPos transpose(World world, BlockPos worldPosition, BlockPos relativePosition) {
        return worldPosition.down(this.yStart);
    }

    @Override
    public boolean place(World worldIn, BlockPos pos, BlockPos relativePosition, IBlockState state, int flags) {
        if(relativePosition.getY() == this.yStart && (state.isFullBlock() || state.getBlock() instanceof BlockFence)) {
            BlockPos mut = pos;
            while (mut.getY() > 0 && !worldIn.isSideSolid(mut.down(), EnumFacing.UP)) {
                worldIn.setBlockState(mut, state, flags);
                mut = mut.down();
            }
        }
        return TemplatePlacement.super.place(worldIn, pos, relativePosition, state, flags);
    }
}
