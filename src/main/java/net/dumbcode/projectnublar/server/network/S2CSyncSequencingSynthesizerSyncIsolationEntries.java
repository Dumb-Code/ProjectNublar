package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.projectnublar.client.gui.machines.DnaEditingScreen;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CSyncSequencingSynthesizerSyncIsolationEntries {

    private final BlockPos pos;
    private final List<SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<?>> entryList;


    public static S2CSyncSequencingSynthesizerSyncIsolationEntries fromBlockEntity(SequencingSynthesizerBlockEntity blockEntity) {
        return new S2CSyncSequencingSynthesizerSyncIsolationEntries(
            blockEntity.getBlockPos(),
            new ArrayList<>(blockEntity.getIsolationOverrides().values())
        );
    }

    public static S2CSyncSequencingSynthesizerSyncIsolationEntries fromBytes(PacketBuffer buf) {
        return new S2CSyncSequencingSynthesizerSyncIsolationEntries(
            buf.readBlockPos(),
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> {
                    GeneticType<?, ?> type = buf.readRegistryIdSafe(GeneticType.getWildcardType());
                    return readValue(type, buf);
                })
                .collect(Collectors.toList())
        );
    }

    private static <O> SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<O> readValue(GeneticType<?, O> type, PacketBuffer buffer) {
        return new SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<>(type, type.getDataHandler().read(buffer));
    }

    public static void toBytes(S2CSyncSequencingSynthesizerSyncIsolationEntries packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeShort(packet.entryList.size());
        for (SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<?> entry : packet.entryList) {
            buf.writeRegistryId(entry.getType());
            writeValue(entry, buf);
        }
    }

    private static <O> void writeValue(SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<O> type, PacketBuffer buffer) {
        type.getType().getDataHandler().write(type.getValue(), buffer);
    }

    public static void handle(S2CSyncSequencingSynthesizerSyncIsolationEntries message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            TileEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos);
            if(blockEntity instanceof SequencingSynthesizerBlockEntity) {
                SequencingSynthesizerBlockEntity entity = (SequencingSynthesizerBlockEntity) blockEntity;
                entity.getIsolationOverrides().clear();
                for (SequencingSynthesizerBlockEntity.IsolatedGeneticEntry<?> entry : message.entryList) {
                    entity.insertIsolationEntry(entry);
                }
                Screen screen = Minecraft.getInstance().screen;
                if(screen instanceof DnaEditingScreen) {
                    ((DnaEditingScreen) screen).onSelectChange();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
