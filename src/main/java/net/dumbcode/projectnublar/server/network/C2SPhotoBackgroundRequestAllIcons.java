package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SPhotoBackgroundRequestAllIcons {

    private final boolean global;

    public static C2SPhotoBackgroundRequestAllIcons fromBytes(PacketBuffer buf) {
        return new C2SPhotoBackgroundRequestAllIcons(buf.readBoolean());
    }

    public static void toBytes(C2SPhotoBackgroundRequestAllIcons packet, PacketBuffer buf) {
        buf.writeBoolean(packet.global);
    }

    public static void handle(C2SPhotoBackgroundRequestAllIcons packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new S2CRequestBackgroundIconHeaders(packet.global, TabletBGImageHandler.getAllIcons(packet.global, context.getSender()))));

        context.setPacketHandled(true);
    }
}
