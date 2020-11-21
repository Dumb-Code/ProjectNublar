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

public class S22StartTrackingTabletHandshake implements IMessage {

    private int startX;
    private int endX;
    private int startZ;
    private int endZ;

    public S22StartTrackingTabletHandshake() {
    }

    public S22StartTrackingTabletHandshake(int startX, int endX, int startZ, int endZ) {
        this.startX = startX;
        this.endX = endX;
        this.startZ = startZ;
        this.endZ = endZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.startX = buf.readInt();
        this.endX = buf.readInt();
        this.startZ = buf.readInt();
        this.endZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.startX);
        buf.writeInt(this.endX);
        buf.writeInt(this.startZ);
        buf.writeInt(this.endZ);
    }

    public static class Handler extends WorldModificationsMessageHandler<S22StartTrackingTabletHandshake, S22StartTrackingTabletHandshake> {

        @Override
        protected void handleMessage(S22StartTrackingTabletHandshake message, MessageContext ctx, World world, EntityPlayer player) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof OpenedTabletScreen) {
                TabletPage tabletScreen = ((OpenedTabletScreen) screen).getScreen();
                if(tabletScreen instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletScreen).initializeSize(message.startX, message.startZ, message.endX - message.startX + 1, message.endZ - message.startZ + 1);
                }
            }
        }
    }

}
