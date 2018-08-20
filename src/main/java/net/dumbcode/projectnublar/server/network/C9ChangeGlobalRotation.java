package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.SkeletalHistory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C9ChangeGlobalRotation implements IMessage {

    private int x;
    private int y;
    private int z;
    private float newRotation;

    public C9ChangeGlobalRotation() { }

    public C9ChangeGlobalRotation(BlockEntitySkeletalBuilder builder, float newRotation) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.newRotation = newRotation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        newRotation = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeFloat(newRotation);
    }

    public static class Handler extends WorldModificationsMessageHandler<C9ChangeGlobalRotation, IMessage> {
        @Override
        protected void handleMessage(C9ChangeGlobalRotation message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = player.world.getTileEntity(pos);
            if(te instanceof BlockEntitySkeletalBuilder) {
                BlockEntitySkeletalBuilder builder = (BlockEntitySkeletalBuilder)te;
                builder.setRotation(message.newRotation);
                builder.markDirty();
                ProjectNublar.NETWORK.sendToAll(new S10ChangeGlobalRotation(builder, message.newRotation));
            }
            pos.release();
        }
    }
}
