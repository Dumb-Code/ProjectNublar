package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.tablet.TabletHomeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class S26OpenTablet implements IMessage {

    private EnumHand hand;

    public S26OpenTablet() {
    }

    public S26OpenTablet(EnumHand hand) {
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.hand = EnumHand.values()[buf.readByte() % EnumHand.values().length];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.hand.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<S26OpenTablet, S26OpenTablet> {

        @Override
        protected void handleMessage(S26OpenTablet message, MessageContext ctx, World world, EntityPlayer player) {
            this.handleGui(message.hand);
        }

        @SideOnly(Side.CLIENT)
        private void handleGui(EnumHand hand) {
            Minecraft.getMinecraft().displayGuiScreen(new TabletHomeGui(hand));
        }
    }
}
