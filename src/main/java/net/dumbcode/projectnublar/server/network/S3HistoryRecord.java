package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S3HistoryRecord implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;

    public S3HistoryRecord() { }

    public S3HistoryRecord(SkeletalBuilderBlockEntity builder, String selectedPart) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.part = selectedPart;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
    }

    public static class Handler extends WorldModificationsMessageHandler<S3HistoryRecord, IMessage> {
        @Override
        protected void handleMessage(S3HistoryRecord message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                builder.getHistory().record(message.part);
            }
            pos.release();
        }
    }
}
