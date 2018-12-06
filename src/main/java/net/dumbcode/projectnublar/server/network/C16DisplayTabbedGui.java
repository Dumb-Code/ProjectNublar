package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C16DisplayTabbedGui implements IMessage {

    private BlockPos pos;
    private int tabId;

    @SuppressWarnings("unused")
    public C16DisplayTabbedGui() {
    }

    public C16DisplayTabbedGui(BlockPos pos, int tabId) {
        this.pos = pos;
        this.tabId = tabId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.tabId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.tabId);
    }

    public static class Handler extends WorldModificationsMessageHandler<C16DisplayTabbedGui, IMessage> {

        @Override
        protected void handleMessage(C16DisplayTabbedGui message, MessageContext ctx, World world, EntityPlayer player) {
            player.openGui(ProjectNublar.INSTANCE, message.tabId, world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
        }
    }
}
