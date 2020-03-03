package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C35RequestAllIcons implements IMessage {

    private boolean global;

    public C35RequestAllIcons() {
    }

    public C35RequestAllIcons(boolean global) {
        this.global = global;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.global = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.global);
    }

    public static class Handler extends WorldModificationsMessageHandler<C35RequestAllIcons, C35RequestAllIcons> {

        @Override
        protected void handleMessage(C35RequestAllIcons message, MessageContext ctx, World world, EntityPlayer player) {
            ProjectNublar.NETWORK.sendTo(new S36RequestBackgroundIconHeaders(message.global, TabletBGImageHandler.getAllIcons(message.global, player)), (EntityPlayerMP) player);
        }
    }
}
