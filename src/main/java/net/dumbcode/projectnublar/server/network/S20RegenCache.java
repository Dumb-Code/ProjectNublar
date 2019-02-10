package net.dumbcode.projectnublar.server.network;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Set;

public class S20RegenCache implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler extends WorldModificationsMessageHandler<S20RegenCache, S20RegenCache> {

        @Override
        protected void handleMessage(S20RegenCache message, MessageContext ctx, World world, EntityPlayer player) {
            for (TileEntity te : world.loadedTileEntityList) {
                if(te instanceof ConnectableBlockEntity) {
                    ConnectableBlockEntity ce = (ConnectableBlockEntity) te;
                    Set<Connection> newConnection = Sets.newLinkedHashSet();
                    for (Connection con : ce.getConnections()) {
                        newConnection.add(con.copy());
                    }
                    ce.getConnections().clear();
                    ce.getConnections().addAll(newConnection);
                }
            }
        }
    }
}
