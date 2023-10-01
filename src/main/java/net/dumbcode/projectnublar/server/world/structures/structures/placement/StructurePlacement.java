package net.dumbcode.projectnublar.server.world.structures.structures.placement;

import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.block.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;

public interface StructurePlacement {

    StructurePlacement EMPTY = (world, instance, worldPosition, relativePosition) -> worldPosition;

    BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition);

    default boolean place(World worldIn, StructureInstance instance, BlockPos pos, BlockPos relativePosition, BlockState state, int flags) {
        return worldIn.setBlock(pos, state, flags);
    }
}
