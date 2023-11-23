package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class S2CSyncOpenedUsers {
    private final BlockPos pos;
    private final Set<UUID> uuidSet;

    public static S2CSyncOpenedUsers fromBytes(FriendlyByteBuf buf) {
        return new S2CSyncOpenedUsers(
            buf.readBlockPos(),
            IntStream.range(0, buf.readShort()).mapToObj(i -> buf.readUUID()).collect(Collectors.toSet())
        );
    }

    public static void toBytes(S2CSyncOpenedUsers packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeShort(packet.uuidSet.size());
        for (UUID uuid : packet.uuidSet) {
            buf.writeUUID(uuid);
        }
    }

    public static void handle(S2CSyncOpenedUsers packet, Supplier<NetworkEvent.Context> supplier) {
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
