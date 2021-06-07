package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S44SyncOpenedUsers {
    private final BlockPos pos;
    private final Set<UUID> uuidSet;

    public static S44SyncOpenedUsers fromBytes(PacketBuffer buf) {
        return new S44SyncOpenedUsers(
            buf.readBlockPos(),
            IntStream.range(0, buf.readShort()).mapToObj(i -> buf.readUUID()).collect(Collectors.toSet())
        );
    }

    public static void toBytes(S44SyncOpenedUsers packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeShort(packet.uuidSet.size());
        for (UUID uuid : packet.uuidSet) {
            buf.writeUUID(uuid);
        }
    }

    public static void handle(S44SyncOpenedUsers packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TileEntity entity = Minecraft.getInstance().level.getBlockEntity(packet.pos);
            if(entity instanceof MachineModuleBlockEntity) {
                Set<UUID> users = ((MachineModuleBlockEntity<?>) entity).getOpenedUsers();
                users.clear();
                users.addAll(packet.uuidSet);
            }
        });

        context.setPacketHandled(true);
    }
}
