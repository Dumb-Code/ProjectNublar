package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.client.gui.tablet.OpenedTabletScreen;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class S2COpenTabletModule {

    private TabletModuleType<?> module;
    private Consumer<FriendlyByteBuf> bufConsumerWrite;

    private TabletScreen screen;

    //Clientside
    public S2COpenTabletModule(TabletScreen screen) {
        this.screen = screen;
    }

    //Serverside
    public S2COpenTabletModule(TabletModuleType<?> module, Consumer<FriendlyByteBuf> bufConsumerWrite) {
        this.module = module;
        this.bufConsumerWrite = bufConsumerWrite;
    }

    public static S2COpenTabletModule fromBytes(FriendlyByteBuf buf) {
        TabletScreen screen = buf.readRegistryIdSafe(TabletModuleType.getWildcardType()).getScreenCreator().apply(buf);
        return new S2COpenTabletModule(screen);
    }

    public static void toBytes(S2COpenTabletModule packet, FriendlyByteBuf buf) {
        buf.writeRegistryId(packet.module);
        packet.bufConsumerWrite.accept(buf);
    }

    public static void handle(S2COpenTabletModule packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if(packet.screen != null) {
                Screen screen = Minecraft.getInstance().screen;
                if(screen instanceof OpenedTabletScreen) {
                    packet.screen.onSetAsCurrentScreen();
                    ((OpenedTabletScreen) screen).setScreen(packet.screen);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
