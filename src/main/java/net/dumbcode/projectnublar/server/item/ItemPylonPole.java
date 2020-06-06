package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.BlockPylonHead;
import net.dumbcode.projectnublar.server.block.BlockPylonPole;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class ItemPylonPole extends ItemBlock {
    public ItemPylonPole() {
        super(BlockHandler.PYLON_POLE);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = worldIn.getBlockState(pos);
        if(state.getBlock() == BlockHandler.PYLON_POLE || state.getBlock() == BlockHandler.PYLON_HEAD) {
            BlockPos testPos = pos;
            while(worldIn.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE) {
                testPos = testPos.up();
            }
            IBlockState head = worldIn.getBlockState(testPos);
            if(head.getBlock() == BlockHandler.PYLON_HEAD && head.getValue(BlockPylonHead.FACING) == EnumFacing.UP) {
                EnumFacing.Axis axis;
                if(worldIn.getBlockState(testPos.down()).getBlock() == BlockHandler.PYLON_POLE) {
                    axis = worldIn.getBlockState(testPos.down()).getValue(BlockPylonPole.AXIS);
                } else {
                    axis = player.getHorizontalFacing().getAxis();
                }
                if(head.getBlock() == BlockHandler.PYLON_HEAD) {
                    TileEntity entity = worldIn.getTileEntity(testPos);
                    worldIn.setBlockState(testPos, BlockHandler.PYLON_POLE.getDefaultState().withProperty(BlockPylonPole.AXIS, axis));
                    worldIn.setBlockState(testPos.up(), BlockHandler.PYLON_HEAD.getDefaultState());
                    if(entity instanceof PylonHeadBlockEntity) {
                        moveConnections((PylonHeadBlockEntity) entity, testPos.up());
                    }
                    if(!player.isCreative()) {
                        player.getHeldItem(hand).shrink(1);
                    }

                }
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    public static void moveConnections(PylonHeadBlockEntity oldTe, BlockPos newPos) {
        World world = oldTe.getWorld();
        TileEntity newEntity = world.getTileEntity(newPos);
        if(newEntity instanceof PylonHeadBlockEntity) {
            ((PylonHeadBlockEntity) newEntity).setNetworkUUID(oldTe.getNetworkUUID());
        }
        for (PylonHeadBlockEntity.Connection connection : oldTe.getConnections()) {
            BlockPos other = connection.getOther(oldTe.getPos());

            TileEntity otherTile = world.getTileEntity(other);
            PylonHeadBlockEntity.Connection newConnection = new PylonHeadBlockEntity.Connection(newPos, other);

            if(otherTile instanceof PylonHeadBlockEntity) {
                Set<PylonHeadBlockEntity.Connection> connections = ((PylonHeadBlockEntity) otherTile).getConnections();
                connections.remove(connection);
                connections.add(newConnection);
                otherTile.markDirty();
                ((PylonHeadBlockEntity) otherTile).syncToClient();
            }

            if(newEntity instanceof PylonHeadBlockEntity) {
                ((PylonHeadBlockEntity) newEntity).getConnections().add(newConnection);
                newEntity.markDirty();
                ((PylonHeadBlockEntity) newEntity).syncToClient();
            }
        }
    }
}
