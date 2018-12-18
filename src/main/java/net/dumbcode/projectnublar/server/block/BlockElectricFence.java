package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockElectricFence extends Block implements IItemBlock {
    public BlockElectricFence() {
        super(Material.IRON, MapColor.IRON);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;

            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            double maxZ = Double.MIN_VALUE;


            for (Connection connection : ((BlockEntityElectricFence) te).fenceConnections) {
                for (int i = 0; i < 2; i++) {
                    double[] in = LineUtils.intersect(pos, connection.getFrom(), connection.getTo(), 0.25+0.5*i);
                    if(in != null) {
                        minX = Math.min(minX, in[0]);
                        maxX = Math.max(maxX, in[1]);

                        minZ = Math.min(minZ, in[2]);
                        maxZ = Math.max(maxZ, in[3]);

                        minY = Math.min(minY, in[4]);
                        maxY = Math.max(maxY, in[5]);
                    }
                }
            }
            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(1/16F);
        }
        return super.getSelectedBoundingBox(state, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            for (AxisAlignedBB bb : ((BlockEntityElectricFence) te).createBoundingBox()) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
            }
            return;
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntityElectricFence();
    }
}
