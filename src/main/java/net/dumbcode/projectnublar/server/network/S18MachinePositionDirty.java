package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S18MachinePositionDirty implements IMessage {

    private BlockPos pos;

    @SuppressWarnings("unused")
    public S18MachinePositionDirty() {
    }

    public S18MachinePositionDirty(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
    }

    public static class Handler extends WorldModificationsMessageHandler<S18MachinePositionDirty, IMessage> {

        @Override
        protected void handleMessage(S18MachinePositionDirty message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity te = world.getTileEntity(message.pos);
            if(te instanceof MachineModuleBlockEntity) {
                ((MachineModuleBlockEntity) te).positionDirty = true;
            }
        }
    }

}
