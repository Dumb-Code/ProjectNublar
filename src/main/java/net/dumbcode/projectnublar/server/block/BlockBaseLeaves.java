package net.dumbcode.projectnublar.server.block;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockBaseLeaves extends BlockLeaves implements IItemBlock  {
    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        throw new IllegalArgumentException("Should not get to this stage");
    }

    @Nonnull
    @Override
    public List<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(this));
    }
}
