package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C31TrackingBeaconNameChanged implements IMessage {

    private BlockPos pos;
    private String name;

    public C31TrackingBeaconNameChanged() {

    }

    public C31TrackingBeaconNameChanged(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        ByteBufUtils.writeUTF8String(buf, this.name);
    }

    public static class Handler extends WorldModificationsMessageHandler<C31TrackingBeaconNameChanged, C31TrackingBeaconNameChanged> {

        @Override
        protected void handleMessage(C31TrackingBeaconNameChanged message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity tileEntity = world.getTileEntity(message.pos);
            if(tileEntity instanceof TrackingBeaconBlockEntity) {
                ((TrackingBeaconBlockEntity) tileEntity).setName(message.name);
            }
        }
    }
}
