package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CSyncSequencingSynthesizerSyncSelected {

    private final BlockPos pos;
    private final SyncDnaEntry[] datas;

    public static S2CSyncSequencingSynthesizerSyncSelected fromBlockEntity(SequencingSynthesizerBlockEntity blockEntity) {
        return new S2CSyncSequencingSynthesizerSyncSelected(
            blockEntity.getBlockPos(),
            IntStream.range(0, 9).mapToObj(i -> new SyncDnaEntry(blockEntity.getSelectKey(i), blockEntity.getSelectAmount(i)))
            .toArray(SyncDnaEntry[]::new)
        );
    }

    public static S2CSyncSequencingSynthesizerSyncSelected fromBytes(PacketBuffer buf) {
        return new S2CSyncSequencingSynthesizerSyncSelected(
            buf.readBlockPos(),
            IntStream.range(0, 9)
                .mapToObj(i -> new SyncDnaEntry(buf.readUtf(), buf.readDouble()))
                .toArray(SyncDnaEntry[]::new)
        );
    }

    public static void toBytes(S2CSyncSequencingSynthesizerSyncSelected packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        for (SyncDnaEntry data : packet.datas) {
            buf.writeUtf(data.key);
            buf.writeDouble(data.amount);
        }
    }

    public static void handle(S2CSyncSequencingSynthesizerSyncSelected message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            TileEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos);
            if(blockEntity instanceof SequencingSynthesizerBlockEntity) {
                for (int i = 0; i < message.datas.length; i++) {
                    SyncDnaEntry data = message.datas[i];
                    ((SequencingSynthesizerBlockEntity) blockEntity).setSelect(i, data.key, data.amount);
                }
            }
        });
        context.setPacketHandled(true);
    }

    @Value
    private static class SyncDnaEntry {
        String key;
        double amount;
    }

}
