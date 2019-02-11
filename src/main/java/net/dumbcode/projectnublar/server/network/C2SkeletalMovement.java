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

import javax.vecmath.Vector3f;

public class C2SkeletalMovement implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private Vector3f rotations;

    public C2SkeletalMovement() { }

    public C2SkeletalMovement(BlockPos pos, String selectedPart, Vector3f rotations) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.part = selectedPart;
        this.rotations = rotations;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        rotations = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeFloat(this.rotations.x);
        buf.writeFloat(this.rotations.y);
        buf.writeFloat(this.rotations.z);
    }

    public static class Handler extends WorldModificationsMessageHandler<C2SkeletalMovement, IMessage> {
        @Override
        public void handleMessage(C2SkeletalMovement message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                builder.getHistory().add(new SkeletalHistory.Record(message.part, message.rotations));
            }
            ProjectNublar.NETWORK.sendToDimension(new S3HistoryRecord(pos, message.part, message.rotations), world.provider.getDimension());

            pos.release();
        }
    }
}
