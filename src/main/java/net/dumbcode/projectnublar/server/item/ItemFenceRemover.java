package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class ItemFenceRemover extends Item {

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
        if(result != null && result.hitInfo instanceof BlockConnectableBase.HitChunk) {
            Connection connection = ((BlockConnectableBase.HitChunk) result.hitInfo).getConnection();
            for (BlockPos blockPos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                TileEntity te = worldIn.getTileEntity(blockPos);
                if(te instanceof ConnectableBlockEntity) {
                    boolean left = false;
                    for (Connection c : ((ConnectableBlockEntity) te).getConnections()) {
                        if(connection.lazyEquals(c)) {
                            c.setBroken(true);
                        } else {
                            left |= !c.isBroken();
                        }
                    }
                    if(!left && ((ConnectableBlockEntity) te).removedByFenceRemovers()) {
                        worldIn.setBlockToAir(blockPos);
                    }
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

}
