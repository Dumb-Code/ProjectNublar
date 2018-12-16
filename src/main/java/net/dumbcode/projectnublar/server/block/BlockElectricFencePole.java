package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class BlockElectricFencePole extends Block implements IItemBlock {
    public BlockElectricFencePole() {
        super(Material.IRON, MapColor.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if(stack.getItem() == Item.getItemFromBlock(BlockHandler.ELECTRIC_FENCE)) { //Move to item class ?
            NBTTagCompound nbt = stack.getOrCreateSubCompound(ProjectNublar.MODID);
            if(nbt.hasKey("fence_position", Constants.NBT.TAG_LONG)) {
                BlockPos other = BlockPos.fromLong(nbt.getLong("fence_position"));
                if(worldIn.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                    TileEntity te = worldIn.getTileEntity(pos);
                    if(te instanceof BlockEntityElectricFencePole) {
                        ((BlockEntityElectricFencePole) te).fenceConnections.add(other);
                    }
                    TileEntity otherte = worldIn.getTileEntity(other);
                    if(otherte instanceof BlockEntityElectricFencePole) {
                        ((BlockEntityElectricFencePole) otherte).fenceConnections.add(pos);
                    }
                    for (BlockPos normalPos : LineUtils.getBlocksInbetween(pos, other, position -> !position.equals(pos) && !position.equals(other))) {
                        for (int i = 0; i < 3; i++) {
                            BlockPos position = normalPos.up(i);
                            if(worldIn.isAirBlock(position)) {
                                worldIn.setBlockState(position, BlockHandler.ELECTRIC_FENCE.getDefaultState());
                            }
                            TileEntity fencete = worldIn.getTileEntity(position);
                            if(fencete instanceof BlockEntityElectricFence) {
                                ((BlockEntityElectricFence) fencete).fenceConnections.add(new BlockEntityElectricFence.Connection(pos.up(i), other.up(i)));
                            }
                        }
                    }

                }
                nbt.removeTag("fence_position");
            } else {
                nbt.setLong("fence_position", pos.toLong());
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntityElectricFencePole();
    }
}
