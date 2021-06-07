package net.dumbcode.projectnublar.server.network;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S43SyncMachineStack {

    private final BlockPos pos;
    private final int slot;
    private final ItemStack stack;

    public S43SyncMachineStack(BlockPos pos, int slot, ItemStack stack) {
        this.pos = pos;
        this.slot = slot;
        this.stack = stack;
    }

    public S43SyncMachineStack(MachineModuleBlockEntity<?> entity, int slot) {
        this.pos = entity.getBlockPos();
        this.slot = slot;
        this.stack = entity.getHandler().getStackInSlot(slot);
    }

    public static S43SyncMachineStack fromBytes(PacketBuffer buf) {
        return new S43SyncMachineStack(
            buf.readBlockPos(), buf.readInt(), buf.readItem()
        );
    }

    public static void toBytes(S43SyncMachineStack packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.slot);
        buf.writeItem(packet.stack);
    }

    public static void handle(S43SyncMachineStack packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            TileEntity te = Minecraft.getInstance().level.getBlockEntity(packet.pos);
            if(te instanceof MachineModuleBlockEntity) {
                ((MachineModuleBlockEntity<?>) te).getHandler().setStackInSlot(packet.slot, packet.stack);
            }
        });

        context.setPacketHandled(true);
    }
}
