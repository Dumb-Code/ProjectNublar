package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2STrackingTabletEntryClicked {

    private final BlockPos pos;

    public static C2STrackingTabletEntryClicked fromBytes(FriendlyByteBuf buf) {
        return new C2STrackingTabletEntryClicked(buf.readBlockPos());
    }

    public static void toBytes(C2STrackingTabletEntryClicked packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(C2STrackingTabletEntryClicked packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            TileEntity tileEntity = sender.level.getBlockEntity(packet.pos);
            if(tileEntity instanceof TrackingBeaconBlockEntity) {
                new TrackingTabletIterator(sender, packet.pos, ((TrackingBeaconBlockEntity) tileEntity).getRadius());
            }
        });

        context.setPacketHandled(true);
    }
}
