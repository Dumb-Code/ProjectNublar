package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.BlockPylonHead;
import net.dumbcode.projectnublar.server.block.BlockPylonPole;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class ItemPylonPole extends BlockItem {
    public ItemPylonPole(Item.Properties properties) {
        super(BlockHandler.PYLON_POLE.get(), properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        ItemStack stack = context.getItemInHand();
        PlayerEntity player = context.getPlayer();

        BlockState state = world.getBlockState(pos);
        if(state.getBlock() == BlockHandler.PYLON_POLE.get() || state.getBlock() == BlockHandler.PYLON_HEAD.get()) {
            BlockPos testPos = pos;
            while(world.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE.get()) {
                testPos = testPos.above();
            }
            BlockState head = world.getBlockState(testPos);
            if(head.getBlock() == BlockHandler.PYLON_HEAD.get() && head.getValue(BlockPylonHead.FACING) == Direction.UP) {
                Direction.Axis axis;
                if(world.getBlockState(testPos.below()).getBlock() == BlockHandler.PYLON_POLE.get()) {
                    axis = world.getBlockState(testPos.below()).getValue(BlockPylonPole.AXIS);
                } else {
                    axis = direction.getAxis();
                }
                if(head.getBlock() == BlockHandler.PYLON_HEAD.get()) {
                    TileEntity entity = world.getBlockEntity(testPos);
                    world.setBlock(testPos, BlockHandler.PYLON_POLE.get().defaultBlockState().setValue(BlockPylonPole.AXIS, axis), 3);
                    world.setBlock(testPos.above(), BlockHandler.PYLON_HEAD.get().defaultBlockState(), 3);
                    if(entity instanceof PylonHeadBlockEntity) {
                        moveConnections((PylonHeadBlockEntity) entity, testPos.above());
                    }
                    if(player != null && !player.isCreative()) {
                        stack.shrink(1);
                    }

                }
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    public static void moveConnections(PylonHeadBlockEntity oldTe, BlockPos newPos) {
        World world = oldTe.getLevel();
        TileEntity newEntity = world.getBlockEntity(newPos);
        if(newEntity instanceof PylonHeadBlockEntity) {
            ((PylonHeadBlockEntity) newEntity).setNetworkUUID(oldTe.getNetworkUUID());
        }
        for (PylonHeadBlockEntity.Connection connection : oldTe.getConnections()) {
            BlockPos other = connection.getOther(oldTe.getBlockPos());

            TileEntity otherTile = world.getBlockEntity(other);
            PylonHeadBlockEntity.Connection newConnection = new PylonHeadBlockEntity.Connection(newPos, other);

            if(otherTile instanceof PylonHeadBlockEntity) {
                Set<PylonHeadBlockEntity.Connection> connections = ((PylonHeadBlockEntity) otherTile).getConnections();
                connections.remove(connection);
                connections.add(newConnection);
                otherTile.setChanged();
                ((PylonHeadBlockEntity) otherTile).syncToClient();
            }

            if(newEntity instanceof PylonHeadBlockEntity) {
                ((PylonHeadBlockEntity) newEntity).getConnections().add(newConnection);
                newEntity.setChanged();
                ((PylonHeadBlockEntity) newEntity).syncToClient();
            }
        }
    }
}
