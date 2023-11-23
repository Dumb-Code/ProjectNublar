package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SConfirmTrackingTablet {

    public static C2SConfirmTrackingTablet fromBytes(FriendlyByteBuf buffer) {
        return new C2SConfirmTrackingTablet();
    }

    public static void toBytes(C2SConfirmTrackingTablet packet, FriendlyByteBuf buffer) {

    }

    public static void handle(C2SConfirmTrackingTablet packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player != null && TrackingTabletIterator.PLAYER_TO_TABLET_MAP.containsKey(player.getUUID())) {
                TrackingTabletIterator.PLAYER_TO_TABLET_MAP.get(player.getUUID()).start();
            }
        });

        context.setPacketHandled(true);
    }
}
