package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S19SetGuiWindow implements IMessage {

    private int windowID;

    @SuppressWarnings("unused")
    public S19SetGuiWindow() {
    }

    public S19SetGuiWindow(int id) {
        this.windowID = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.windowID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.windowID);
    }

    public static class Handler extends WorldModificationsMessageHandler<S19SetGuiWindow, IMessage> {

        @Override
        protected void handleMessage(S19SetGuiWindow message, MessageContext ctx, World world, EntityPlayer player) {
            player.openContainer.windowId = message.windowID;
        }
    }
}
