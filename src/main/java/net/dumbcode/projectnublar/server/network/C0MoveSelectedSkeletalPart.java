package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.utils.RotationAxis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;

public class C0MoveSelectedSkeletalPart implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private RotationAxis axis;
    private float newAngle;

    public C0MoveSelectedSkeletalPart() { }

    public C0MoveSelectedSkeletalPart(SkeletalBuilderBlockEntity builder, String selectedPart, RotationAxis axis, float newAngle) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.part = selectedPart;
        this.axis = axis;
        this.newAngle = newAngle;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        axis = RotationAxis.values()[buf.readInt()];
        newAngle = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeInt(axis.ordinal());
        buf.writeFloat(newAngle);
    }

    public static class Handler extends WorldModificationsMessageHandler<C0MoveSelectedSkeletalPart, IMessage> {
        @Override
        public void handleMessage(C0MoveSelectedSkeletalPart message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)te;
                if(!builder.getPoseData().containsKey(message.part)) {
                    builder.getPoseData().put(message.part, new Vector3f());
                }
                Vector3f angles = builder.getPoseData().get(message.part);
                switch (message.axis) {
                    case X_AXIS:
                        angles.x = message.newAngle;
                        break;
                    case Y_AXIS:
                        angles.y = message.newAngle;
                        break;
                    case Z_AXIS:
                        angles.z = message.newAngle;
                        break;
                }
                builder.markDirty();
                ProjectNublar.NETWORK.sendToAll(new S1UpdateSkeletalBuilder(builder, message.part, angles));
            }

            pos.release();
        }
    }
}
