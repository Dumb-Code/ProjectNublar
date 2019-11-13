package net.dumbcode.projectnublar.server.world.structures.structures.placement;

import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface StructurePlacement {

    StructurePlacement EMPTY = (world, instance, worldPosition, relativePosition) -> worldPosition;

    BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition);

    default boolean place(World worldIn, StructureInstance instance, BlockPos pos, BlockPos relativePosition, IBlockState state, int flags) {
        return worldIn.setBlockState(pos, state, flags);
    }
}
