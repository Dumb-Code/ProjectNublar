package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.GuiTrackingTablet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S22OpenTrackingTabletGui implements IMessage {

    private int startX;
    private int endX;
    private int startZ;
    private int endZ;

    public S22OpenTrackingTabletGui() {
    }

    public S22OpenTrackingTabletGui(int startX, int endX, int startZ, int endZ) {
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

    public static class Handler extends WorldModificationsMessageHandler<S22OpenTrackingTabletGui, S22OpenTrackingTabletGui> {

        @Override
        protected void handleMessage(S22OpenTrackingTabletGui message, MessageContext ctx, World world, EntityPlayer player) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiTrackingTablet(message.startX, message.startZ, message.endX - message.startX + 1, message.endZ - message.startZ + 1));
        }
    }

}
