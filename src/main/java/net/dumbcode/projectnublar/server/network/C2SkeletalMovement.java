package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalHistory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C2SkeletalMovement implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private SkeletalHistory.MovementType type;

    public C2SkeletalMovement() { }

    public C2SkeletalMovement(SkeletalBuilderBlockEntity builder, String selectedPart, SkeletalHistory.MovementType type) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.part = selectedPart;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        type = SkeletalHistory.MovementType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeInt(type.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<C2SkeletalMovement, IMessage> {
        @Override
        public void handleMessage(C2SkeletalMovement message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                if(message.type == SkeletalHistory.MovementType.STOPPING) {
                    builder.getHistory().record(message.part);
                    ProjectNublar.NETWORK.sendToAll(new S3HistoryRecord(builder, message.part));
                } else {
                    builder.getHistory().prepareForRecording(message.part);
                }
            }

            pos.release();
        }
    }
}
