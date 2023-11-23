package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SSequencingSynthesizerIsolationChange {

    private final SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<?> entry;

    public static C2SSequencingSynthesizerIsolationChange fromBytes(FriendlyByteBuf buf) {
        GeneticType<?, ?> type = buf.readRegistryIdSafe(GeneticType.getWildcardType());
        return new C2SSequencingSynthesizerIsolationChange(readValue(type, buf));
    }

    private static <O> SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<O> readValue(GeneticType<?, O> type, FriendlyByteBuf buffer) {
        return new SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<>(type, type.getDataHandler().read(buffer));
    }

    public static void toBytes(C2SSequencingSynthesizerIsolationChange packet, FriendlyByteBuf buf) {
        buf.writeRegistryId(packet.entry.getType());
        writeValue(packet.entry, buf);
    }

    private static <O> void writeValue(SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<O> type, FriendlyByteBuf buffer) {
        type.getType().getDataHandler().write(type.getValue(), buffer);
    }

    public static void handle(C2SSequencingSynthesizerIsolationChange message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        MachineModuleContainer.runWhenOnMenu(context, SequencingSynthesizerBlockEntity.class, b -> {
            if(!b.isProcessingMain()) {
                b.insertIsolationEntry(message.entry);
                ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(context.getSender().level::dimension), S2CSyncSequencingSynthesizerSyncIsolationEntries.fromBlockEntity(b));
            }
        });
        context.setPacketHandled(true);
    }

}
