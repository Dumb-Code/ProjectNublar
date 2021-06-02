package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.TabletHomeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2COpenTablet {

    private final Hand hand;

    public static S2COpenTablet fromBytes(PacketBuffer buf) {
        return new S2COpenTablet(buf.readEnum(Hand.class));
    }

    public static void toBytes(S2COpenTablet packet, PacketBuffer buf) {
        buf.writeEnum(packet.hand);
    }

    public static void handle(S2COpenTablet packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new TabletHomeGui(packet.hand));
        });
        context.setPacketHandled(true);

    }

}
