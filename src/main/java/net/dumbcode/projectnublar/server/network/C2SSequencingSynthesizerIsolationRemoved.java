package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SSequencingSynthesizerIsolationRemoved {

    private final GeneticType<?, ?> type;

    public static C2SSequencingSynthesizerIsolationRemoved fromBytes(FriendlyByteBuf buf) {
        return new C2SSequencingSynthesizerIsolationRemoved(buf.readRegistryIdSafe(GeneticType.getWildcardType()));
    }

    public static void toBytes(C2SSequencingSynthesizerIsolationRemoved packet, FriendlyByteBuf buf) {
        buf.writeRegistryId(packet.type);
    }

    public static void handle(C2SSequencingSynthesizerIsolationRemoved message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        MachineModuleContainer.runWhenOnMenu(context, SequencingSynthesizerBlockEntity.class, b -> {
            if(!b.isProcessingMain()) {
                b.removeIsolationEntry(message.type);
                ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(context.getSender().level::dimension), S2CSyncSequencingSynthesizerSyncIsolationEntries.fromBlockEntity(b));
            }
        });
        context.setPacketHandled(true);
    }

}
