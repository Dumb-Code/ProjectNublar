package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.inventory.container.Container;
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

    public static void handle(C2SSequencingSynthesizerSelectChange message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> MachineModuleContainer.getFromMenu(
            SequencingSynthesizerBlockEntity.class,
            context.getSender()).ifPresent(b -> b.setSelect(message.id, message.key, message.amount))
        );
        context.setPacketHandled(true);
    }

}
