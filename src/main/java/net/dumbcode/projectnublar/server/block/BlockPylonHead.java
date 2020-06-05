package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class BlockPylonHead extends Block implements IItemBlock {
    public BlockPylonHead() {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack item = playerIn.getHeldItem(hand);
        if(item.getItem() == ItemHandler.WIRE_SPOOL) {
            NBTTagCompound nbt = item.getOrCreateSubCompound(ProjectNublar.MODID);
            if(nbt.hasKey("pylon_position", Constants.NBT.TAG_LONG)) {
                BlockPos other = BlockPos.fromLong(nbt.getLong("pylon_position"));
                if(!pos.equals(other)) {
                    PylonHeadBlockEntity.Connection connection = new PylonHeadBlockEntity.Connection(other, pos);

                    TileEntity entity = worldIn.getTileEntity(pos);
                    if(entity instanceof PylonHeadBlockEntity) {
                        ((PylonHeadBlockEntity) entity).addConnection(connection);
                    }

                    TileEntity otherEntity = worldIn.getTileEntity(other);
                    if(otherEntity instanceof PylonHeadBlockEntity) {
                        ((PylonHeadBlockEntity) otherEntity).addConnection(connection);
                    }
                }
                nbt.removeTag("pylon_position");
            } else {
                nbt.setLong("pylon_position", pos.toLong());
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
        return new PylonHeadBlockEntity();
    }
}
