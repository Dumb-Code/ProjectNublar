package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class C34UploadImage implements IMessage {

    private boolean global;
    private byte[] data;

    public C34UploadImage() {
    }

    public C34UploadImage(boolean global, BufferedImage image) {
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
        this.global = buf.readBoolean();
        this.data = new byte[buf.readInt()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.global);
        buf.writeInt(this.data.length);
        buf.writeBytes(this.data);
    }

    public static class Handler extends WorldModificationsMessageHandler<C34UploadImage, C34UploadImage> {

        private static MessageDigest SHA1 = getSHA1();

        @Override
        protected void handleMessage(C34UploadImage message, MessageContext ctx, World world, EntityPlayer player) {
            try {
                @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(message.data);
                @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
                TabletBGImageHandler.addNewEntry(player, ImageIO.read(gzis), hash(message.data));
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to sync image from client", e);
            }
            ProjectNublar.NETWORK.sendTo(new S36RequestBackgroundIconHeaders(message.global, TabletBGImageHandler.getAllIcons(message.global, player)), (EntityPlayerMP) player);
        }

        private static String hash(byte[] convertme) {
            return byteArray2Hex(SHA1.digest(convertme));
        }

        private static String byteArray2Hex(final byte[] hash) {
            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }

        @SneakyThrows(NoSuchAlgorithmException.class)
        private static MessageDigest getSHA1() {
            return MessageDigest.getInstance("SHA-1");
        }
    }
}
