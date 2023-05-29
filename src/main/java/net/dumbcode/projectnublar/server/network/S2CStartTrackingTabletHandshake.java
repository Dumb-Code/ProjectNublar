package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2CStartTrackingTabletHandshake {

    private final int startX;
    private final int endX;
    private final int startZ;
    private final int endZ;

    public static S2CStartTrackingTabletHandshake fromBytes(PacketBuffer buf) {
        return new S2CStartTrackingTabletHandshake(
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()
        );
    }

    public static void toBytes(S2CStartTrackingTabletHandshake packet, PacketBuffer buf) {
        buf.writeInt(packet.startX);
        buf.writeInt(packet.endX);
        buf.writeInt(packet.startZ);
        buf.writeInt(packet.endZ);
    }

    public static void handle(S2CStartTrackingTabletHandshake packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof OpenedTabletScreen) {
                TabletPage tabletPage = ((OpenedTabletScreen) screen).getScreen();
                if(tabletPage instanceof TrackingTabletScreen) {
                    ((TrackingTabletScreen) tabletPage).initializeSize(packet.startX, packet.startZ, packet.endX - packet.startX + 1, packet.endZ - packet.startZ + 1);
                }
            }
        });

        context.setPacketHandled(true);
    }

}
