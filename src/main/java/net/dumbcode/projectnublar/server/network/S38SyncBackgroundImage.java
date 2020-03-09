package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundableScreen;
import net.dumbcode.projectnublar.server.tablet.backgrounds.PhotoBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class S38SyncBackgroundImage implements IMessage {

    private byte[] data;

    public S38SyncBackgroundImage() {
    }

    public S38SyncBackgroundImage(BufferedImage image) {
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
        this.data = new byte[buf.readInt()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.data.length);
        buf.writeBytes(this.data);
    }

    public static class Handler extends WorldModificationsMessageHandler<S38SyncBackgroundImage, S38SyncBackgroundImage> {


        @Override
        protected void handleMessage(S38SyncBackgroundImage message, MessageContext ctx, World world, EntityPlayer player) {
            try {
                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if(screen instanceof BackgroundableScreen) {
                    @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(message.data);
                    @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
                    BufferedImage image = ImageIO.read(gzis);

                    BackgroundableScreen bgs = (BackgroundableScreen) screen;
                    if(!(bgs.getBackground() instanceof PhotoBackground)) {
                        bgs.setBackground(new PhotoBackground());
                    }
                    ((PhotoBackground)bgs.getBackground()).setTexture(new DynamicTexture(image));
                }
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to sync image from client", e);
            }
        }
    }
}
