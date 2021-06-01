package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.Random;

public class BlockElectricFence extends BlockConnectableBase {

    public static final int ITEM_FOLD = 20;

    public BlockElectricFence(Properties properties) {
        super(properties);
    }


    @Override
    public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                if(connection.isBroken() || !connection.isPowered(world)) {
                    continue;
                 }
                Vector3d center = connection.getCenter();

                boolean pb = connection.brokenSide(world, false);

                float chance = 0.02F;

                if(pb || connection.brokenSide(world, true) && rand.nextFloat() < chance) {
                    spawnParticles(world, (pb ? connection.getPrevCache() : connection.getNextCache()).getPoint(), center);
                }
            }
        }
        super.randomDisplayTick(stateIn, world, pos, rand);
    }

    private void spawnParticles(World world,Vector3d point, Vector3d center) {
        Vector3d norm = new Vector3d();
        norm.normalize(point);
        ProjectNublar.spawnParticles(ParticleType.SPARKS, world, center.x+point.x, center.y+point.y, center.z+point.z, norm.x,norm.y,norm.z, 8);
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
