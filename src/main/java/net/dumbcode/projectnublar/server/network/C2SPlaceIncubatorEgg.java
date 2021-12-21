package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.UnincubatedEggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SPlaceIncubatorEgg {

    private final int xPos;
    private final int yPos;

    public static C2SPlaceIncubatorEgg fromBytes(PacketBuffer buf) {
        return new C2SPlaceIncubatorEgg(
            MathHelper.clamp(buf.readInt(), IncubatorBlockEntity.HALF_EGG_SIZE, IncubatorBlockEntity.BED_WIDTH-IncubatorBlockEntity.HALF_EGG_SIZE),
            MathHelper.clamp(buf.readInt(), IncubatorBlockEntity.HALF_EGG_SIZE, IncubatorBlockEntity.BED_HEIGHT-IncubatorBlockEntity.HALF_EGG_SIZE)
        );
    }

    public static void toBytes(C2SPlaceIncubatorEgg packet, PacketBuffer buf) {
        buf.writeInt(packet.xPos);
        buf.writeInt(packet.yPos);
    }

    public static void handle(C2SPlaceIncubatorEgg packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        
        MachineModuleContainer.runWhenOnMenu(context, IncubatorBlockEntity.class, i -> {
            ItemStack stack = context.getSender().inventory.getCarried();
            if (stack.getItem() instanceof UnincubatedEggItem) {
                i.placeEgg(packet.xPos, packet.yPos, stack);
                context.getSender().broadcastCarriedItem();
                i.syncToClient();
            }
        });

        context.setPacketHandled(true);
    }
}
