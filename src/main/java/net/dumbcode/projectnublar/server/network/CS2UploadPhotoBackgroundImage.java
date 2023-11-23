package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class CS2UploadPhotoBackgroundImage {

    private static MessageDigest SHA1 = getSHA1();

    private final boolean global;
    private final byte[] data;

    public static CS2UploadPhotoBackgroundImage fromBytes(FriendlyByteBuf buf) {
        return new CS2UploadPhotoBackgroundImage(
            buf.readBoolean(),
            buf.readByteArray(buf.readInt())
        );
    }

    public static void toBytes(CS2UploadPhotoBackgroundImage packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.global);
        buf.writeInt(packet.data.length);
        buf.writeByteArray(packet.data);
    }

    public static void handle(CS2UploadPhotoBackgroundImage packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            try {
                TabletBGImageHandler.addNewEntry(sender, NativeImage.read(new ByteArrayInputStream(packet.data)), hash(packet.data));
                ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new S2CRequestBackgroundIconHeaders(packet.global, TabletBGImageHandler.getAllIcons(packet.global, context.getSender())));
            } catch (IOException e) {
                ProjectNublar.LOGGER.error("Unable to read image.");
            }
        });

        context.setPacketHandled(true);
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
