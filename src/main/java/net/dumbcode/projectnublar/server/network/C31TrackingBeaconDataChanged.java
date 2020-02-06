package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C31TrackingBeaconDataChanged implements IMessage {

    private BlockPos pos;
    private String name;
    private int radius;

    public C31TrackingBeaconDataChanged() {
    }

    public C31TrackingBeaconDataChanged(BlockPos pos, String name, int radius) {
        this.pos = pos;
        this.name = name;
        this.radius = radius;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.name = ByteBufUtils.readUTF8String(buf);
        this.radius = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        ByteBufUtils.writeUTF8String(buf, this.name);
        buf.writeInt(this.radius);
    }

    public static class Handler extends WorldModificationsMessageHandler<C31TrackingBeaconDataChanged, C31TrackingBeaconDataChanged> {

        @Override
        protected void handleMessage(C31TrackingBeaconDataChanged message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity tileEntity = world.getTileEntity(message.pos);
            if(tileEntity instanceof TrackingBeaconBlockEntity) {
                TrackingBeaconBlockEntity te = (TrackingBeaconBlockEntity) tileEntity;
                te.setName(message.name);
                te.setRadius(message.radius);
            }
        }
    }
}
