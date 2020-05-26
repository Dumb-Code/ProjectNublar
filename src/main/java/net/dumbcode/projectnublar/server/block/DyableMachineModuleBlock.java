package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.DyableBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.MachineModulePart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class DyableMachineModuleBlock extends MachineModuleBlock {
    public DyableMachineModuleBlock(Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier, MachineModulePart... values) {
        super(machineSupplier, values);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        TileEntity entity = worldIn.getTileEntity(pos);
        if(stack.getItem() == Items.DYE && entity instanceof DyableBlockEntity) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(stack.getMetadata());
            if(((DyableBlockEntity) entity).getDye() != color) {
                ((DyableBlockEntity) entity).setDye(color);
                entity.markDirty();
                worldIn.markBlockRangeForRenderUpdate(pos, pos);
                if(!playerIn.isCreative()) {
                    stack.shrink(1);
                }
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
