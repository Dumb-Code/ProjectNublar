package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SManualStartRecipe {
    private final int processId;

    public static C2SManualStartRecipe fromBytes(PacketBuffer buf) {
        return new C2SManualStartRecipe(buf.readByte());
    }

    public static void toBytes(C2SManualStartRecipe packet, PacketBuffer buf) {
        buf.writeByte(packet.processId);
    }

    public static void handle(C2SManualStartRecipe packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Container menu = context.getSender().containerMenu;
            if(menu instanceof MachineModuleContainer) {
                ((MachineModuleContainer) menu).getBlockEntity().searchForRecipes(packet.processId, true);
            }
        });
        context.setPacketHandled(true);
    }

}
