package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C25StopTrackingTablet {
    public static C25StopTrackingTablet fromBytes(FriendlyByteBuf buffer) {
        return new C25StopTrackingTablet();
    }

    public static void toBytes(C25StopTrackingTablet packet, FriendlyByteBuf buffer) {

    }

    public static void handle(C25StopTrackingTablet packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null && TrackingTabletIterator.PLAYER_TO_TABLET_MAP.containsKey(player.getUUID())) {
                TrackingTabletIterator.PLAYER_TO_TABLET_MAP.get(player.getUUID()).finish();
            }
        });

        context.setPacketHandled(true);
    }
}
