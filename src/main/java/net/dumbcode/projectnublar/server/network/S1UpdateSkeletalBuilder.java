package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;

public class S1UpdateSkeletalBuilder implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private float rx;
    private float ry;
    private float rz;

    public S1UpdateSkeletalBuilder() { }

    public S1UpdateSkeletalBuilder(BlockEntitySkeletalBuilder builder, String selectedPart, Vector3f rotations) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.part = selectedPart;
        this.rx = rotations.x;
        this.ry = rotations.y;
        this.rz = rotations.z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        rx = buf.readFloat();
        ry = buf.readFloat();
        rz = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeFloat(rx);
        buf.writeFloat(ry);
        buf.writeFloat(rz);
    }

    public static class Handler extends WorldModificationsMessageHandler<S1UpdateSkeletalBuilder, IMessage> {
        @Override
        protected void handleMessage(S1UpdateSkeletalBuilder message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BlockEntitySkeletalBuilder) {
                BlockEntitySkeletalBuilder builder = (BlockEntitySkeletalBuilder)te;
                if(!builder.getPoseData().containsKey(message.part)) {
                    builder.getPoseData().put(message.part, new Vector3f());
                }
                Vector3f angles = builder.getPoseData().get(message.part);
                angles.x = message.rx;
                angles.y = message.ry;
                angles.z = message.rz;
            }
            pos.release();
        }
    }
}
