package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.BlockTrackingBeacon;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2STrackingBeaconData {

    private final String name;
    private final int radius;

    public static C2STrackingBeaconData fromBytes(FriendlyByteBuf buf) {
        return new C2STrackingBeaconData(buf.readUtf(), buf.readInt());
    }

    public static void toBytes(C2STrackingBeaconData packet, FriendlyByteBuf buff) {
        buff.writeUtf(packet.name);
        buff.writeInt(packet.radius);
    }

    public static void handle(C2STrackingBeaconData packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ProjectNublarContainers.getFromMenu(BlockTrackingBeacon.TrackingContainer.class, context.getSender())
            .ifPresent(container -> {
                TrackingBeaconBlockEntity beacon = container.getBeacon();
                beacon.setName(packet.name);
                beacon.setRadius(packet.radius);
            })
        );
        context.setPacketHandled(true);
    }
}
