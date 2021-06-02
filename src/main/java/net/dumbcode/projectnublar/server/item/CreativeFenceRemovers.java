package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.state.IBlockState;
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

import java.util.Collection;
import java.util.Set;

public class CreativeFenceRemovers extends Item {

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
        if(result != null && result.hitInfo instanceof BlockConnectableBase.HitChunk) {
            Connection c = ((BlockConnectableBase.HitChunk) result.hitInfo).getConnection();
            BlockPos origin = c.getFrom();
            IBlockState baseState = worldIn.getBlockState(origin);
            if(!(baseState.getBlock() instanceof BlockElectricFencePole)) {
                origin = c.getTo();
                baseState = worldIn.getBlockState(origin);
            }
            if(baseState.getBlock() instanceof BlockElectricFencePole) {
                BlockPos o = pos.down(baseState.getValue(((BlockElectricFencePole) baseState.getBlock()).indexProperty));
                Set<ConnectableBlockEntity> set = Sets.newHashSet();
                for (int i = 0; i < ((BlockElectricFencePole) baseState.getBlock()).getType().getHeight(); i++) {
                    listConnections(worldIn.getTileEntity(o.up(i)), set);
                }
                //Remove all the fence blocks. Maybe we should only remove the connection?
                set.stream()
                    .flatMap(cbe -> cbe.getConnections().stream())
                    .map(connection -> LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset()))
                    .flatMap(Collection::stream)
                    .forEach(worldIn::setBlockToAir);
            }
            return EnumActionResult.SUCCESS;
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    private void listConnections(TileEntity te, Set<ConnectableBlockEntity> set) {
        if(te instanceof ConnectableBlockEntity) {
            set.add((ConnectableBlockEntity) te);
            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                BlockPos other = connection.getFrom().equals(connection.getPosition()) ? connection.getTo() : connection.getFrom();
                TileEntity o = te.getWorld().getTileEntity(other);
                if(o instanceof ConnectableBlockEntity && !set.contains((ConnectableBlockEntity) o)) {
                    listConnections(o, set);
                }
            }
        }
    }
}
