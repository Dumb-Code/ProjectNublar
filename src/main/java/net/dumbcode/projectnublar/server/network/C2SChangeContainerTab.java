package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
    public class C2SChangeContainerTab {

    private final int tab;
    private final BlockPos pos;


    public static C2SChangeContainerTab fromBytes(PacketBuffer buf) {
        return new C2SChangeContainerTab(buf.readInt(), buf.readBlockPos());
    }

    public static void toBytes(C2SChangeContainerTab packet, PacketBuffer buf) {
        buf.writeInt(packet.tab);
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(C2SChangeContainerTab packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            TileEntity blockEntity = sender.level.getBlockEntity(packet.pos);
            if(blockEntity instanceof MachineModuleBlockEntity && isBlockEntityInChain(sender, (MachineModuleBlockEntity<?>) blockEntity)) {
                ((MachineModuleBlockEntity<?>) blockEntity).openContainer(sender, packet.tab);
            }
        });

        context.setPacketHandled(true);
    }

    private static boolean isBlockEntityInChain(ServerPlayerEntity entity, MachineModuleBlockEntity<?> te) {
        if(entity.containerMenu instanceof MachineModuleContainer) {
            List<MachineModuleBlockEntity<?>> list = new ArrayList<>();
            ((MachineModuleContainer) entity.containerMenu).getBlockEntity().getSurroundings(list);
            return list.contains(te);
        }
        return false;
    }
}
