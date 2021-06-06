package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2CMachinePositionDirty {

    private final BlockPos pos;

    public S2CMachinePositionDirty fromBytes(PacketBuffer buf) {
        return new S2CMachinePositionDirty(buf.readBlockPos());
    }

    public static void toBytes(S2CMachinePositionDirty packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(S2CMachinePositionDirty packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TileEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(packet.pos);
            if(blockEntity instanceof MachineModuleBlockEntity) {
                ((MachineModuleBlockEntity<?>) blockEntity).setPositionDirty(true);
            }
        });

        context.setPacketHandled(true);
    }
}
