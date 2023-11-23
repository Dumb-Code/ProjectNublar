package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.BackgroundTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.PhotoBackgroundSetup;
import net.dumbcode.projectnublar.client.gui.tablet.setuppages.SetupPage;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CRequestBackgroundIconHeaders {

    private final boolean global;
    private final List<TabletBGImageHandler.IconEntry> entryList;

    public static S2CRequestBackgroundIconHeaders fromBytes(FriendlyByteBuf buf) {
        return new S2CRequestBackgroundIconHeaders(
          buf.readBoolean(),
            IntStream.range(0, buf.readShort()).mapToObj(i ->
                new TabletBGImageHandler.IconEntry(buf.readUtf(), buf.readUtf())
            ).collect(Collectors.toList())
        );
    }

    public static void toBytes(S2CRequestBackgroundIconHeaders packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.global);
        buf.writeShort(packet.entryList.size());
        for (TabletBGImageHandler.IconEntry entry : packet.entryList) {
            buf.writeUtf(entry.getUploaderUUID());
            buf.writeUtf(entry.getImageHash());
        }
    }

    public static void handle(S2CRequestBackgroundIconHeaders packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if(screen instanceof BackgroundTabletScreen) {
                SetupPage<?> page = ((BackgroundTabletScreen) screen).getSetupPage();
                if(page instanceof PhotoBackgroundSetup) {
                    ((PhotoBackgroundSetup) page).loadEntries(packet.global, packet.entryList);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
