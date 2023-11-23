package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSetTabletBackground {

    private final InteractionHand hand;
    private final TabletBackground background;

    public C2SSetTabletBackground(InteractionHand hand, TabletBackground background) {
        this.hand = hand;
        this.background = background;
    }

    public static C2SSetTabletBackground fromBytes(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        TabletBackground.Entry<?> entry = TabletBackground.REGISTRY.get(name);
        if(entry != null) {
            TabletBackground background = entry.getBackground();
            background.readFromBuf(buf);
            return new C2SSetTabletBackground(buf.readEnum(Hand.class), background);
        } else {
            throw new IllegalStateException("Error, no background with identifier '" + name + "'");
        }

    }

    public static void toBytes(C2SSetTabletBackground packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.background.identifier());
        packet.background.writeToBuf(buf);
        buf.writeEnum(packet.hand);
    }

    public static void handle(C2SSetTabletBackground packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            try (TabletItemStackHandler handler = new TabletItemStackHandler(context.getSender().getItemInHand(packet.hand))) {
                handler.setBackground(packet.background);
            }
        });

        context.setPacketHandled(true);
    }
}
