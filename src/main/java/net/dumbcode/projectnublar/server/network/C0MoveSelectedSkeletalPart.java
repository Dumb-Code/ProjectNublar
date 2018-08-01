package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;

public class C0MoveSelectedSkeletalPart implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private float dx;
    private float dy;

    public C0MoveSelectedSkeletalPart() { }

    public C0MoveSelectedSkeletalPart(BlockEntitySkeletalBuilder builder, String selectedPart, float dx, float dy) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.part = selectedPart;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        dx = buf.readFloat();
        dy = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeFloat(dx);
        buf.writeFloat(dy);
    }

    public static class Handler extends WorldModificationsMessageHandler<C0MoveSelectedSkeletalPart, IMessage> {
        @Override
        public void handleMessage(C0MoveSelectedSkeletalPart message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BlockEntitySkeletalBuilder) {
                BlockEntitySkeletalBuilder builder = (BlockEntitySkeletalBuilder)te;
                if(!builder.getPoseData().containsKey(message.part)) {
                    builder.getPoseData().put(message.part, new Vector3f());
                }
                Vector3f angles = builder.getPoseData().get(message.part);
                // TODO: better rotations (in camera space?)
                angles.y += message.dx;
                angles.x += message.dy;
                builder.markDirty();
                ProjectNublar.NETWORK.sendToAll(new S1UpdateSkeletalBuilder(builder, message.part, angles));
            }

            pos.release();
        }
    }
}
