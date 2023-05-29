package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class S2COpenTabletModule {

    private TabletModuleType<?> module;
    private Consumer<PacketBuffer> bufConsumerWrite;

    private TabletPage screen;
    private String route;

    //Clientside
    public S2COpenTabletModule(TabletPage screen, String route) {
        this.screen = screen;
        this.route = route;
    }

    //Serverside
    public S2COpenTabletModule(TabletModuleType<?> module, String route, Consumer<PacketBuffer> bufConsumerWrite) {
        this.module = module;
        this.route = route;
        this.bufConsumerWrite = bufConsumerWrite;
    }

    public static S2COpenTabletModule fromBytes(PacketBuffer buf) {
        TabletPage screen = buf.readRegistryIdSafe(TabletModuleType.getWildcardType()).getScreenCreator().apply(buf);
        String route = buf.readUtf();
        return new S2COpenTabletModule(screen, route);
    }

    public static void toBytes(S2COpenTabletModule packet, PacketBuffer buf) {
        buf.writeRegistryId(packet.module);
        buf.writeUtf(packet.route);
        packet.bufConsumerWrite.accept(buf);
    }

    public static void handle(S2COpenTabletModule packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if(packet.screen != null) {
                Screen screen = Minecraft.getInstance().screen;
                if(screen instanceof OpenedTabletScreen) {
                    packet.screen.onSetAsCurrentScreen();
                    ((OpenedTabletScreen) screen).setScreen(packet.screen, packet.route);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
