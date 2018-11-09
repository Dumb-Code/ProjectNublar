package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C17TabbedGuiClicked implements IMessage {

    private BlockPos pos;

    @SuppressWarnings("unused")
    public C17TabbedGuiClicked() {
    }

    public C17TabbedGuiClicked(BlockPos pos) {
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

    public static class Handler extends WorldModificationsMessageHandler<C17TabbedGuiClicked, IMessage> {

        @Override
        protected void handleMessage(C17TabbedGuiClicked message, MessageContext ctx, World world, EntityPlayer player) {
            player.openGui(ProjectNublar.INSTANCE, 0, world, message.pos.getX(), message.pos.getY(), message.pos.getZ());
        }
    }
}
