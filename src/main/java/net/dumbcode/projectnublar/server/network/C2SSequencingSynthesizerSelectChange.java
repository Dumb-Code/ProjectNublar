package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SSequencingSynthesizerSelectChange {

    private final int id;
    private final String key;
    private final double amount;


    public static C2SSequencingSynthesizerSelectChange fromBytes(PacketBuffer buf) {
        return new C2SSequencingSynthesizerSelectChange(
            buf.readInt(), buf.readUtf(), buf.readDouble()
        );
    }

    public static void toBytes(C2SSequencingSynthesizerSelectChange packet, PacketBuffer buf) {
        buf.writeInt(packet.id);
        buf.writeUtf(packet.key);
        buf.writeDouble(packet.amount);
    }

    public static void handle(C2SVehicleInputStateUpdated message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            //TODO: synthesize container getter:
            //context.getSender().containerMenu
        });
        context.setPacketHandled(true);
    }

}
