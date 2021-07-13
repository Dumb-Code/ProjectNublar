package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CSyncMachineProcesses {

    private final BlockPos pos;
    private final List<ProcessSync> processList;
    private final int energy;
    private final int maxEnergy;
    private final int[] extraData;

    public static S2CSyncMachineProcesses fromBytes(PacketBuffer buf) {
        return new S2CSyncMachineProcesses(
            buf.readBlockPos(),
            IntStream.range(0, buf.readByte()).mapToObj(i ->
                new ProcessSync(
                    buf.readShort(), buf.readShort(),
                    buf.readBoolean(), buf.readBoolean(),
                    IntStream.range(0, buf.readByte())
                        .mapToObj(i2 -> buf.readVarIntArray())
                        .collect(Collectors.toList())
                )
            ).collect(Collectors.toList()),
            buf.readInt(),
            buf.readInt(),
            buf.readVarIntArray()
        );
    }

    public static void toBytes(S2CSyncMachineProcesses packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.processList.size());
        for (ProcessSync sync : packet.processList) {
            buf.writeShort(sync.getTime());
            buf.writeShort(sync.getTotalTime());
            buf.writeBoolean(sync.isProcessing());
            buf.writeBoolean(sync.isHasPower());
            buf.writeByte(sync.getBlocked().size());
            for (int[] ints : sync.getBlocked()) {
                buf.writeVarIntArray(ints);
            }
        }
        buf.writeInt(packet.energy);
        buf.writeInt(packet.maxEnergy);
        buf.writeVarIntArray(packet.extraData);
    }

    public static void handle(S2CSyncMachineProcesses packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().level.getBlockEntity(packet.pos);
            if(te instanceof MachineModuleBlockEntity) {
                MachineModuleBlockEntity<?> entity = (MachineModuleBlockEntity<?>) te;
                for (int i = 0; i < packet.processList.size(); i++) {
                    MachineModuleBlockEntity.MachineProcess<?> process = entity.getProcess(i);
                    ProcessSync sync = packet.processList.get(i);
                    process.setTime(sync.getTime());
                    process.setTotalTime(sync.getTotalTime());
                    process.setProcessing(sync.processing && sync.hasPower);
                    process.setHasPower(sync.hasPower);

                    List<MachineModuleBlockEntity.BlockedProcess> blocked = process.getBlockedProcessList();
                    blocked.clear();
                    for (int[] ints : sync.blocked) {
                        blocked.add(new MachineModuleBlockEntity.BlockedProcess(ItemStack.EMPTY, ints));
                    }
                }
                entity.setClientEnergyHeld(packet.energy);
                entity.setClientMaxEnergy(packet.maxEnergy);
                entity.onExtraSyncData(packet.extraData);
            }
        });

        context.setPacketHandled(true);
    }

    @Value
    public static class ProcessSync {
        int time, totalTime;
        boolean processing, hasPower;
        List<int[]> blocked;
        public static ProcessSync of(MachineModuleBlockEntity.MachineProcess<?> process) {
            return new ProcessSync(
                process.getTime(), process.getTotalTime(),
                process.isProcessing(), process.isHasPower(),
                process.getBlockedProcessList().stream()
                    .map(MachineModuleBlockEntity.BlockedProcess::getSlots)
                    .collect(Collectors.toList())
            );
        }
    }
}
