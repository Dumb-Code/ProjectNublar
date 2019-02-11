package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S5UpdateHistoryIndex implements IMessage {

    private int x;
    private int y;
    private int z;
    private int direction;

    public S5UpdateHistoryIndex() { }

    public S5UpdateHistoryIndex(BlockPos pos, int direction) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.direction = direction > 0 ? +1 : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        direction = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(direction);
    }

    public static class Handler extends WorldModificationsMessageHandler<S5UpdateHistoryIndex, IMessage> {
        @Override
        protected void handleMessage(S5UpdateHistoryIndex message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                if(message.direction > 0) {
                    builder.getHistory().redo();
                } else if(message.direction < 0) {
                    builder.getHistory().undo();
                }
            }

            pos.release();
        }
    }
}
