package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.dumbcode.projectnublar.server.utils.TrackingTabletIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class C30TrackingTabletEntryClicked implements IMessage {

    private BlockPos pos;

    public C30TrackingTabletEntryClicked() {

    }

    public C30TrackingTabletEntryClicked(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
    }

    public static class Handler extends WorldModificationsMessageHandler<C30TrackingTabletEntryClicked, C30TrackingTabletEntryClicked> {

        @Override
        protected void handleMessage(C30TrackingTabletEntryClicked message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity tileEntity = world.getTileEntity(message.pos);
            if(tileEntity instanceof TrackingBeaconBlockEntity) {
                new TrackingTabletIterator((EntityPlayerMP) player, message.pos, 150);
            }
        }
    }
}
