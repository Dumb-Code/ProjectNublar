package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C33SetTabletBackground implements IMessage {

    private EnumHand hand;
    private TabletBackground background;

    public C33SetTabletBackground() {
    }

    public C33SetTabletBackground(EnumHand hand, TabletBackground background) {
        this.hand = hand;
        this.background = background;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        String name = ByteBufUtils.readUTF8String(buf);
        TabletBackground.Entry<?> entry = TabletBackground.REGISTRY.get(name);
        if(entry != null) {
            this.background = entry.getBackgroundSupplier().get();
            this.background.readFromBuf(buf);
            this.hand = EnumHand.values()[buf.readByte() % 2];
        } else {
            ProjectNublar.getLogger().error("Error, no background with identifier '{}'", name);
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.background.identifier());
        this.background.writeToBuf(buf);
        buf.writeByte(this.hand.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<C33SetTabletBackground, C33SetTabletBackground> {

        @Override
        protected void handleMessage(C33SetTabletBackground message, MessageContext ctx, World world, EntityPlayer player) {
            try (TabletItemStackHandler handler = new TabletItemStackHandler(player.getHeldItem(message.hand))) {
                handler.setBackground(message.background);
            }
        }
    }
}
