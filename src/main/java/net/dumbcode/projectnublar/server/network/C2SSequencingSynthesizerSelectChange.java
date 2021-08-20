package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.network.NetworkUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class C2SSequencingSynthesizerSelectChange {

    private final int id;
    private final String key;
    private final double amount;
    private final SequencingSynthesizerBlockEntity.DnaColourStorage storage;

    public static C2SSequencingSynthesizerSelectChange fromBytes(PacketBuffer buf) {
        return new C2SSequencingSynthesizerSelectChange(
            buf.readInt(), buf.readUtf(), buf.readDouble(),
            new SequencingSynthesizerBlockEntity.DnaColourStorage(
                IntStream.range(0, buf.readShort()).mapToObj(i -> (int) buf.readByte()).collect(Collectors.toSet()),
                IntStream.range(0, buf.readShort()).mapToObj(i -> (int) buf.readByte()).collect(Collectors.toSet())
            )
        );
    }

    public static void toBytes(C2SSequencingSynthesizerSelectChange packet, PacketBuffer buf) {
        buf.writeInt(packet.id);
        buf.writeUtf(packet.key);
        buf.writeDouble(packet.amount);

        Set<Integer> primary = packet.storage.getPrimary();
        buf.writeShort(primary.size());
        for (int i : primary) {
            buf.writeByte(i);
        }

        Set<Integer> secondary = packet.storage.getSecondary();
        buf.writeShort(secondary.size());
        for (int i : secondary) {
            buf.writeByte(i);
        }
    }

    public static void handle(C2SSequencingSynthesizerSelectChange message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        MachineModuleContainer.runWhenOnMenu(context, SequencingSynthesizerBlockEntity.class, b -> {
            b.setAndValidateSelect(message.id, message.key, message.amount);
            b.setStorage(message.id, message.storage);
            ProjectNublar.NETWORK.send(NetworkUtils.forPos(b.getLevel(), b.getBlockPos()), S2CSyncSequencingSynthesizerSyncSelected.fromBlockEntity(b));
        });
        context.setPacketHandled(true);
    }

}
