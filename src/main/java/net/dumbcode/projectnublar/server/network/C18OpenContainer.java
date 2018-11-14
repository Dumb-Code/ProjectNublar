package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C18OpenContainer implements IMessage {

    private int ID;
    private BlockPos pos;

    @SuppressWarnings("unused")
    public C18OpenContainer() {
    }

    public C18OpenContainer(int id, BlockPos pos) {
        this.ID = id;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.ID = buf.readInt();
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.ID);
        buf.writeLong(this.pos.toLong());
    }

    public static class Handler extends WorldModificationsMessageHandler<C18OpenContainer, IMessage> {

        @Override
        protected void handleMessage(C18OpenContainer message, MessageContext ctx, World world, EntityPlayer player) {
            GuiHandler.INSTANCE.openAndSyncContainer(message.ID, (EntityPlayerMP) player, world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
        }
    }
}
