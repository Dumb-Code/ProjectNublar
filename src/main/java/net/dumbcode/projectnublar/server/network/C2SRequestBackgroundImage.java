package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SRequestBackgroundImage {

    private final String uploaderUUID;
    private final String imageHash;

    public static C2SRequestBackgroundImage fromBytes(PacketBuffer buf) {
        return new C2SRequestBackgroundImage(buf.readUtf(), buf.readUtf());
    }

    public static void toBytes(C2SRequestBackgroundImage packet, PacketBuffer buf) {
        buf.writeUtf(packet.uploaderUUID);
        buf.writeUtf(packet.imageHash);
    }

    public static void handle(C2SRequestBackgroundImage packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            World world = context.getSender().level;
            TabletBGImageHandler.getFullImage(world, packet.uploaderUUID, packet.imageHash, false).ifPresent(image -> {
                try {
                    ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new S2CSyncBackgroundImage(image.asByteArray()));
                } catch (IOException e) {
                    ProjectNublar.getLogger().error("Unable to make image array", e);
                }
            });
        });

        context.setPacketHandled(true);
    }
}
