package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C4MoveInHistory implements IMessage {

    private int x;
    private int y;
    private int z;
    private int direction;

    public C4MoveInHistory() { }

    public C4MoveInHistory(SkeletalBuilderBlockEntity builder, int direction) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
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

    public static class Handler extends WorldModificationsMessageHandler<C4MoveInHistory, IMessage> {

        @Override
        protected void handleMessage(C4MoveInHistory message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                if(message.direction > 0) {
                    builder.getHistory().redo();
                } else if(message.direction < 0) {
                    builder.getHistory().undo();
                }
                ProjectNublar.NETWORK.sendToDimension(new S5UpdateHistoryIndex(pos, message.direction), world.provider.getDimension());
            }

            pos.release();
        }
    }
}
