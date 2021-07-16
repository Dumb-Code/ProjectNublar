package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SSequencerSynthesizerContainerSlotOpened {
    private final int slot;

    public static C2SSequencerSynthesizerContainerSlotOpened fromBytes(PacketBuffer buf) {
        return new C2SSequencerSynthesizerContainerSlotOpened(buf.readByte());
    }

    public static void toBytes(C2SSequencerSynthesizerContainerSlotOpened packet, PacketBuffer buf) {
        buf.writeByte(packet.slot);
    }

    public static void handle(C2SSequencerSynthesizerContainerSlotOpened packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Container menu = context.getSender().containerMenu;
            if(menu instanceof MachineModuleContainer) {
                if(packet.slot == -1) {
                    ((MachineModuleContainer) menu).setPredicate(value -> true);
                } else {
                    int slot = packet.slot;
                    ((MachineModuleContainer) menu).setPredicate(value -> value == slot);
                }

            }
        });

        context.setPacketHandled(true);
    }

}


