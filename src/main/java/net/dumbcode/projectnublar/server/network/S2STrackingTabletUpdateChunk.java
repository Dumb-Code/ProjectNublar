package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2STrackingTabletUpdateChunk {

    private final int startX;
    private final int endX;
    private final int startZ;
    private final int endZ;
    private final int[] data;

    public static S2STrackingTabletUpdateChunk fromBytes(PacketBuffer buf) {
        return new S2STrackingTabletUpdateChunk(
            buf.readInt(), buf.readInt(),
            buf.readInt(), buf.readInt(),
            IntStream.range(0, buf.readInt()).map(i -> buf.readInt()).toArray()
        );
    }

    public static void toBytes(S2STrackingTabletUpdateChunk packet, PacketBuffer buf) {
        buf.writeInt(packet.startX);
        buf.writeInt(packet.endX);
        buf.writeInt(packet.startZ);
        buf.writeInt(packet.endZ);

        buf.writeInt(packet.data.length);
        for (int datum : packet.data) {
            buf.writeInt(datum);
        }
    }

    public static void handle(S2STrackingTabletUpdateChunk packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof OpenedTabletScreen) {
                TabletScreen tabletScreen = ((OpenedTabletScreen) screen).getScreen();
                if(tabletScreen instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletScreen).setRGB(packet.startX, packet.startZ, packet.endX - packet.startX + 1, packet.endZ - packet.startZ + 1, packet.data);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
