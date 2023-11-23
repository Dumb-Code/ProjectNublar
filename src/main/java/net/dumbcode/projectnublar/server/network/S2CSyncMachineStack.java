package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncMachineStack {

    private final BlockPos pos;
    private final int slot;
    private final ItemStack stack;

    public S2CSyncMachineStack(BlockPos pos, int slot, ItemStack stack) {
        this.pos = pos;
        this.slot = slot;
        this.stack = stack;
    }

    public S2CSyncMachineStack(MachineModuleBlockEntity<?> entity, int slot) {
        this.pos = entity.getBlockPos();
        this.slot = slot;
        this.stack = entity.getHandler().getStackInSlot(slot);
    }

    public static S2CSyncMachineStack fromBytes(FriendlyByteBuf buf) {
        return new S2CSyncMachineStack(
            buf.readBlockPos(), buf.readInt(), buf.readItem()
        );
    }

    public static void toBytes(S2CSyncMachineStack packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.slot);
        buf.writeItem(packet.stack);
    }

    public static void handle(S2CSyncMachineStack packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().level.getBlockEntity(packet.pos);
            if(te instanceof MachineModuleBlockEntity) {
                ((MachineModuleBlockEntity<?>) te).getHandler().setStackInSlot(packet.slot, packet.stack);
                Screen screen = Minecraft.getInstance().screen;
                if(screen instanceof MachineContainerScreen) {
                    ((MachineContainerScreen) screen).onSlotChanged(packet.slot, packet.stack);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
