package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

public class ItemFenceRemover extends Item {

    public ItemFenceRemover(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        Object info = context.hitResult.hitInfo;
        if(info instanceof BlockConnectableBase.HitChunk) {
            Connection connection = ((BlockConnectableBase.HitChunk) info).getConnection();
            for (BlockPos blockPos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                TileEntity te = context.getLevel().getBlockEntity(blockPos);
                if (te instanceof ConnectableBlockEntity) {
                    boolean left = false;
                    for (Connection c : ((ConnectableBlockEntity) te).getConnections()) {
                        if (connection.lazyEquals(c)) {
                            c.setBroken(true);
                        } else {
                            left |= !c.isBroken();
                        }
                    }
                    if (!left && ((ConnectableBlockEntity) te).removedByFenceRemovers()) {
                        context.getLevel().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

}
