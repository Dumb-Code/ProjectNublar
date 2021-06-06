package net.dumbcode.projectnublar.server.block;

import lombok.Getter;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class BlockTrackingBeacon extends Block implements IItemBlock {

    public BlockTrackingBeacon(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TrackingBeaconBlockEntity();
    }

    @Override
    public void destroy(IWorld worldIn, BlockPos pos, BlockState state) {
        if(worldIn instanceof ServerWorld) {
            TrackingBeaconBlockEntity.getTrackingList((ServerWorld) worldIn).remove(pos);
        }
        super.destroy(worldIn, pos, state);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof TrackingBeaconBlockEntity && player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (id, inv, p) -> new TrackingContainer(id, (TrackingBeaconBlockEntity) te),
                new StringTextComponent("")
            ), pos);
        }
        return super.use(state, world, pos, player, hand, ray);
    }

    @Getter
    public static class TrackingContainer extends Container {

        private final TrackingBeaconBlockEntity beacon;

        public TrackingContainer(int id, TrackingBeaconBlockEntity beacon) {
            super(ProjectNublarContainers.TRACKING_BEACON.get(), id);
            this.beacon = beacon;
        }

        @Override
        public boolean stillValid(PlayerEntity p_75145_1_) {
            return true;
        }
    }
}
