package net.dumbcode.projectnublar.server.block;

import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.dumbcode.projectnublar.client.gui.GuiSkeletalBuilder;
import net.dumbcode.projectnublar.client.gui.GuiTrackingBeacon;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTrackingBeacon extends Block implements IItemBlock {
    public BlockTrackingBeacon() {
        super(Material.IRON);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TrackingBeaconBlockEntity();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TrackingBeaconBlockEntity.getTrackingList(worldIn).remove(pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TrackingBeaconBlockEntity && te.getWorld().isRemote) {
            SidedExecutor.runClient(() -> () -> Minecraft.getMinecraft().displayGuiScreen(new GuiTrackingBeacon((TrackingBeaconBlockEntity)te)));
        }
        return true;
    }
}
