package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
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
}
