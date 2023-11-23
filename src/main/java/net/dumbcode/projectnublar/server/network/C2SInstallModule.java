package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SInstallModule {

    private final int slot;
    private final InteractionHand hand;


    public static C2SInstallModule fromBytes(FriendlyByteBuf buf) {
        return new C2SInstallModule(
            buf.readByte(), buf.readEnum(Hand.class)
        );
    }

    public static void toBytes(C2SInstallModule packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.slot);
        buf.writeEnum(packet.hand);
    }

    public static void handle(C2SInstallModule packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            ItemStack stack = sender.inventory.getItem(packet.slot);
            if(stack.getItem() instanceof ModuleItem) {
                try (TabletItemStackHandler handler = new TabletItemStackHandler(sender.getItemInHand(packet.hand))) {
                    handler.addNew(((ModuleItem) stack.getItem()).getType());
                }
            }
        });

        context.setPacketHandled(true);
    }

}
