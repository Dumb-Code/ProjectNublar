package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class C28ModuleClicked implements IMessage {

    private ResourceLocation module;
    private EnumHand hand;

    public C28ModuleClicked() {

    }

    public C28ModuleClicked(ResourceLocation module, EnumHand hand) {
        this.module = module;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.module = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        this.hand = EnumHand.values()[buf.readByte() % 2];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.module.toString());
        buf.writeByte(this.hand.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<C28ModuleClicked, C28ModuleClicked> {

        @Override
        protected void handleMessage(C28ModuleClicked message, MessageContext ctx, World world, EntityPlayer player) {
            try (TabletItemStackHandler handler = new TabletItemStackHandler(player.getHeldItem(message.hand))) {
                handler.getEntryList().stream().filter(e -> Objects.equals(e.getType().getRegistryName(), message.module)).findAny().ifPresent(e -> {
                    ProjectNublar.NETWORK.sendTo(new S29OpenTabletModule(e.getType(), e.onOpenScreen((EntityPlayerMP) player)), (EntityPlayerMP) player);
                });
            }
        }
    }
}
