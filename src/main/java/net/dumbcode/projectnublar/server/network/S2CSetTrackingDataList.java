package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CSetTrackingDataList {

    private final List<TrackingSavedData.DataEntry> entries;

    public static S2CSetTrackingDataList fromBytes(FriendlyByteBuf buf) {
        return new S2CSetTrackingDataList(
            IntStream.range(0, buf.readShort()).mapToObj(i -> TrackingSavedData.DataEntry.deserailize(buf)).collect(Collectors.toList())
        );
    }

    public static void toBytes(S2CSetTrackingDataList packet, FriendlyByteBuf buf) {
        buf.writeShort(packet.entries.size());
        packet.entries.forEach(entry -> TrackingSavedData.DataEntry.serialize(buf, entry));
    }

    public static void handle(S2CSetTrackingDataList packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof OpenedTabletScreen) {
                TabletScreen tabletScreen = ((OpenedTabletScreen) screen).getScreen();
                if (tabletScreen instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletScreen).setTrackingData(packet.entries);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
