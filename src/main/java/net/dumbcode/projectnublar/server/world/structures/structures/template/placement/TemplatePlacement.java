package net.dumbcode.projectnublar.server.world.structures.structures.template.placement;

import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TemplatePlacement {
    BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition);

    default boolean place(World worldIn, StructureInstance instance, BlockPos pos, BlockPos relativePosition, IBlockState state, int flags) {
        return worldIn.setBlockState(pos, state, flags);
    }

    default void loadData(NBTTemplate template) {
    }

    class EmptyPlacement implements TemplatePlacement {

        @Override
        public BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition) {
            return worldPosition;
        }
    }
}
