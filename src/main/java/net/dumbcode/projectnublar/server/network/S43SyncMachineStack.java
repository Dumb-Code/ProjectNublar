package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S43SyncMachineStack implements IMessage {

    private BlockPos pos;
    private int slot;
    private ItemStack stack;

    public S43SyncMachineStack() {
    }

    public S43SyncMachineStack(MachineModuleBlockEntity<?> entity, int slot) {
        this.pos = entity.getPos();
        this.slot = slot;
        this.stack = entity.getHandler().getStackInSlot(slot);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.slot = buf.readByte();
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeByte(this.slot);
        ByteBufUtils.writeItemStack(buf, this.stack);
    }


    public static class Handler extends WorldModificationsMessageHandler<S43SyncMachineStack, S43SyncMachineStack> {

        @Override
        protected void handleMessage(S43SyncMachineStack message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity te = world.getTileEntity(message.pos);
            if(te instanceof MachineModuleBlockEntity) {
                ((MachineModuleBlockEntity<?>) te).getHandler().setStackInSlot(message.slot, message.stack);
            }

        }
    }
}
