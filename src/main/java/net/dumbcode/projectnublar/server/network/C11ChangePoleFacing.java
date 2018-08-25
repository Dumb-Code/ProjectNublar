package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C11ChangePoleFacing implements IMessage {

    private int x;
    private int y;
    private int z;
    private PoleFacing newFacing;

    public C11ChangePoleFacing() { }

    public C11ChangePoleFacing(BlockEntitySkeletalBuilder builder, PoleFacing newFacing) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.newFacing = newFacing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        newFacing = PoleFacing.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(newFacing.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<C11ChangePoleFacing, IMessage> {
        @Override
        protected void handleMessage(C11ChangePoleFacing message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = player.world.getTileEntity(pos);
            if(te instanceof BlockEntitySkeletalBuilder) {
                BlockEntitySkeletalBuilder builder = (BlockEntitySkeletalBuilder)te;
                builder.getSkeletalProperties().setPoleFacing(message.newFacing);
                builder.markDirty();
                ProjectNublar.NETWORK.sendToAll(new S12ChangePoleFacing(builder, message.newFacing));
            }
            pos.release();
        }
    }
}
