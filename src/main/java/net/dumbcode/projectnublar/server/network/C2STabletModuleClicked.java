package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2STabletModuleClicked {

    private final ResourceLocation module;
    private final Hand hand;

    public static C2STabletModuleClicked fromBytes(PacketBuffer buf) {
        return new C2STabletModuleClicked(
            buf.readResourceLocation(),
            buf.readEnum(Hand.class)
        );
    }

    public static void toBytes(C2STabletModuleClicked packet, PacketBuffer buf) {
        buf.writeResourceLocation(packet.module);
        buf.writeEnum(packet.hand);
    }

    public static void handle(C2STabletModuleClicked packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            try (TabletItemStackHandler handler = new TabletItemStackHandler(context.getSender().getItemInHand(packet.hand))) {
                handler.getEntryList().stream().filter(e -> Objects.equals(e.getType().getRegistryName(), packet.module)).findAny().ifPresent(e ->
                    ProjectNublar.NETWORK.send(PacketDistributor.PLAYER.with(context::getSender), new S2COpenTabletModule(e.getType(), e.onOpenScreen(context.getSender())))
                );
            }
        });

        context.setPacketHandled(true);
    }
}
