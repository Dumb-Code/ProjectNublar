package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SRequestPhotoBackgroundIcon {

    private final boolean global;
    private final String uploaderUUID;
    private final String imageHash;

    public static C2SRequestPhotoBackgroundIcon fromBytes(PacketBuffer buf) {
        return new C2SRequestPhotoBackgroundIcon(
            buf.readBoolean(),
            buf.readUtf(),
            buf.readUtf()
        );
    }

    public static void toBytes(C2SRequestPhotoBackgroundIcon packet, PacketBuffer buf) {
        buf.writeBoolean(packet.global);
        buf.writeUtf(packet.uploaderUUID);
        buf.writeUtf(packet.imageHash);
    }

    public static void handle(C2SRequestPhotoBackgroundIcon packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TabletBGImageHandler.getFullImage(context.getSender().level, packet.uploaderUUID, packet.imageHash, true).ifPresent(image -> {
                byte[] bytes = new byte[0];
                try {
                    bytes = image.asByteArray();
                } catch (IOException e) {
                    ProjectNublar.LOGGER.error("Unable to write image to array", e);
                }
                ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new S2CSyncBackgroundIcon(packet.uploaderUUID, packet.imageHash, packet.global, bytes));
            });
        });

        context.setPacketHandled(true);
    }
}
