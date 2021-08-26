package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SManualStopRecipe {
    private final int processId;

    public static C2SManualStopRecipe fromBytes(PacketBuffer buf) {
        return new C2SManualStopRecipe(buf.readByte());
    }

    public static void toBytes(C2SManualStopRecipe packet, PacketBuffer buf) {
        buf.writeByte(packet.processId);
    }

    public static void handle(C2SManualStopRecipe packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Container menu = context.getSender().containerMenu;
            if(menu instanceof MachineModuleContainer) {
                MachineModuleBlockEntity.MachineProcess<?> process = ((MachineModuleContainer) menu).getBlockEntity().getProcess(packet.processId);
                process.setProcessing(false);
                process.setTotalTime(0);
                process.setTime(0);
                process.setCurrentRecipe(null);
            }
        });
        context.setPacketHandled(true);
    }

}
