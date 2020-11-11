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

public class C28LinkClicked implements IMessage {

    private ResourceLocation module;
    private EnumHand hand;
    private String route;

    public C28LinkClicked() {

    }

    public C28LinkClicked(ResourceLocation module, EnumHand hand) {
        this.module = module;
        this.hand = hand;
        this.route = "";
    }
    
    public C28LinkClicked(ResourceLocation module, EnumHand hand, String route) {
        this.module = module;
        this.hand = hand;
        this.route = route;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.module = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        this.hand = EnumHand.values()[buf.readByte() % 2];
        this.route = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.module.toString());
        buf.writeByte(this.hand.ordinal());
        ByteBufUtils.writeUTF8String(buf, this.route);
    }

    public static class Handler extends WorldModificationsMessageHandler<C28LinkClicked, C28LinkClicked> {

        @Override
        protected void handleMessage(C28LinkClicked message, MessageContext ctx, World world, EntityPlayer player) {
            try (TabletItemStackHandler handler = new TabletItemStackHandler(player.getHeldItem(message.hand))) {
                handler.getEntryList().stream().filter(e -> Objects.equals(e.getType().getRegistryName(), message.module)).findAny().ifPresent(e -> {
                    ProjectNublar.NETWORK.sendTo(new S29OpenTabletModule(e.getType(), e.getType().getRegistryName().toString().split(":")[1].concat(":").concat(message.route), e.onOpenScreen((EntityPlayerMP) player)), (EntityPlayerMP) player);
                });
            }
        }
    }
}
