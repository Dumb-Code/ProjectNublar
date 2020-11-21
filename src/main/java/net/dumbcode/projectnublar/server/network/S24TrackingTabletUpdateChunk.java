package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S24TrackingTabletUpdateChunk implements IMessage {

    private int startX;
    private int endX;
    private int startZ;
    private int endZ;
    private int[] data;

    public S24TrackingTabletUpdateChunk() {
    }

    public S24TrackingTabletUpdateChunk(int startX, int endX, int startZ, int endZ, int[] data) {
        this.startX = startX;
        this.endX = endX;
        this.startZ = startZ;
        this.endZ = endZ;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.startX = buf.readInt();
        this.endX = buf.readInt();
        this.startZ = buf.readInt();
        this.endZ = buf.readInt();

        int len = buf.readInt();
        this.data = new int[len];
        for (int i = 0; i < len; i++) {
            this.data[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.startX);
        buf.writeInt(this.endX);
        buf.writeInt(this.startZ);
        buf.writeInt(this.endZ);

        buf.writeInt(this.data.length);
        for (int datum : this.data) {
            buf.writeInt(datum);
        }
    }

    public static class Handler extends WorldModificationsMessageHandler<S24TrackingTabletUpdateChunk, S24TrackingTabletUpdateChunk> {

        @Override
        protected void handleMessage(S24TrackingTabletUpdateChunk message, MessageContext ctx, World world, EntityPlayer player) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof OpenedTabletScreen) {
                TabletPage tabletScreen = ((OpenedTabletScreen) screen).getScreen();
                if(tabletScreen instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletScreen).setRGB(message.startX, message.startZ, message.endX - message.startX + 1, message.endZ - message.startZ + 1, message.data);
                }
            }
        }
    }
}
