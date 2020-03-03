package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundTabletScreen;
import net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages.PhotoBackgroundSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class S39SyncBackgroundIcon implements IMessage {

    private String uploaderUUID;
    private String imageHash;
    private boolean global;
    private byte[] data;

    public S39SyncBackgroundIcon() {
    }

    public S39SyncBackgroundIcon(String uploaderUUID, String imageHash, boolean global, BufferedImage image) {
        this.uploaderUUID = uploaderUUID;
        this.imageHash = imageHash;
        this.global = global;
        try {
            @Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            ImageIO.write(image, "PNG", gzos);
            gzos.close();
            this.data = baos.toByteArray();
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to sync image to server", e);
            this.data = new byte[0];
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uploaderUUID = ByteBufUtils.readUTF8String(buf);
        this.imageHash = ByteBufUtils.readUTF8String(buf);
        this.global = buf.readBoolean();
        this.data = new byte[buf.readInt()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uploaderUUID);
        ByteBufUtils.writeUTF8String(buf, this.imageHash);
        buf.writeBoolean(this.global);
        buf.writeInt(this.data.length);
        buf.writeBytes(this.data);
    }

    public static class Handler extends WorldModificationsMessageHandler<S39SyncBackgroundIcon, S39SyncBackgroundIcon> {

        @Override
        protected void handleMessage(S39SyncBackgroundIcon message, MessageContext ctx, World world, EntityPlayer player) {
            try {
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if(screen instanceof BackgroundTabletScreen) {
                    BackgroundTabletScreen tabletScreen = (BackgroundTabletScreen) screen;
                    if(tabletScreen.getSetupPage() instanceof PhotoBackgroundSetup) {
                        @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(message.data);
                        @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
                        BufferedImage image = ImageIO.read(gzis);
                        ((PhotoBackgroundSetup) tabletScreen.getSetupPage()).loadIcon(message.uploaderUUID, message.imageHash, message.global, image);
                    }
                }
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to sync image from client", e);
            }
        }
    }
}
