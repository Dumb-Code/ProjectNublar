package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Set;

public class CreativeFenceRemovers extends Item {

    public CreativeFenceRemovers(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        Object info = context.hitResult.hitInfo;
        if(info instanceof BlockConnectableBase.HitChunk) {
            World world = context.getLevel();
            Connection c = ((BlockConnectableBase.HitChunk) info).getConnection();

            BlockPos origin = c.getFrom();
            BlockState baseState = world.getBlockState(origin);
            if(!(baseState.getBlock() instanceof BlockElectricFencePole)) {
                origin = c.getTo();
                baseState = world.getBlockState(origin);
            }
            if(baseState.getBlock() instanceof BlockElectricFencePole) {
                BlockPos o = context.getClickedPos().below(baseState.getValue(((BlockElectricFencePole) baseState.getBlock()).indexProperty));
                Set<ConnectableBlockEntity> set = Sets.newHashSet();
                for (int i = 0; i < ((BlockElectricFencePole) baseState.getBlock()).getType().getHeight(); i++) {
                    listConnections(world.getBlockEntity(o.above(i)), set);
                }
                //Remove all the fence blocks. Maybe we should only remove the connection?
                set.stream()
                    .flatMap(cbe -> cbe.getConnections().stream())
                    .map(connection -> LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset()))
                    .flatMap(Collection::stream)
                    .forEach(pos -> world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3));
            }
            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    private void listConnections(TileEntity te, Set<ConnectableBlockEntity> set) {
        if(te instanceof ConnectableBlockEntity) {
            set.add((ConnectableBlockEntity) te);
            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                BlockPos other = connection.getFrom().equals(connection.getPosition()) ? connection.getTo() : connection.getFrom();
                TileEntity o = te.getLevel().getBlockEntity(other);
                if(o instanceof ConnectableBlockEntity && !set.contains((ConnectableBlockEntity) o)) {
                    listConnections(o, set);
                }
            }
        }
    }
}
