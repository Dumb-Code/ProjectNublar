package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.particles.ProjectNublarParticles;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockElectricFence extends BlockConnectableBase {

    public static final int ITEM_FOLD = 20;

    public BlockElectricFence(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public void animateTick(BlockState stateIn, World world, BlockPos pos, Random rand) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                if(connection.isBroken() || !connection.isPowered(world)) {
                    continue;
                 }
                Vector3d center = connection.getCenter();

                boolean pb = connection.brokenSide(world, false);

                float chance = 0.02F;

                if(pb || connection.brokenSide(world, true) && rand.nextFloat() < chance) {
                    Vector3f point = (pb ? connection.getPrevCache() : connection.getNextCache()).getPoint();
                    Vector3f norm = new Vector3f(point.x(), point.y(), point.z());
                    if(norm.normalize()) {
                        for (int i = 0; i < 8; i++) {
                            world.addParticle(ProjectNublarParticles.SPARK.get(),
                                center.x+point.x(), center.y+point.y(), center.z+point.z(),
                                norm.x(), norm.y(), norm.z()
                            );
                        }
                    }
                }
            }
        }
        super.animateTick(stateIn, world, pos, rand);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockEntityElectricFence();
    }
}
