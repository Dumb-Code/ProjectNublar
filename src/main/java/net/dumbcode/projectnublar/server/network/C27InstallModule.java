package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C27InstallModule implements IMessage {

    private int slot;
    private EnumHand hand;

    public C27InstallModule() {
    }

    public C27InstallModule(int slot, EnumHand hand) {
        this.slot = slot;
        this.hand = hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readByte();
        this.hand = EnumHand.values()[buf.readByte() % 2];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.slot);
        buf.writeByte(this.hand.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<C27InstallModule, C27InstallModule> {

        @Override
        protected void handleMessage(C27InstallModule message, MessageContext ctx, World world, EntityPlayer player) {
            ItemStack stack = player.inventory.getStackInSlot(message.slot);
            if(stack.getItem() instanceof ModuleItem) {
                try (TabletItemStackHandler handler = new TabletItemStackHandler(player.getHeldItem(message.hand))) {
                    handler.addNew(((ModuleItem) stack.getItem()).getType());
                }
            }
        }
    }
}
