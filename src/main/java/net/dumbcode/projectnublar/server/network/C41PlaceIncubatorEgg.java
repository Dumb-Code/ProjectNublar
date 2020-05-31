package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C41PlaceIncubatorEgg implements IMessage {

    private BlockPos pos;
    private int xPos;
    private int yPos;

    public C41PlaceIncubatorEgg() {
    }

    public C41PlaceIncubatorEgg(BlockPos pos, int xPos, int yPos) {
        this.pos = pos;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.xPos = MathHelper.clamp(buf.readInt(), IncubatorBlockEntity.HALF_EGG_SIZE, 100-IncubatorBlockEntity.HALF_EGG_SIZE);
        this.yPos = MathHelper.clamp(buf.readInt(), IncubatorBlockEntity.HALF_EGG_SIZE, 100-IncubatorBlockEntity.HALF_EGG_SIZE);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.xPos);
        buf.writeInt(this.yPos);
    }

    public static class Handler extends WorldModificationsMessageHandler<C41PlaceIncubatorEgg, C41PlaceIncubatorEgg> {

        @Override
        protected void handleMessage(C41PlaceIncubatorEgg message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity te = world.getTileEntity(message.pos);
            if(te instanceof IncubatorBlockEntity) {
                IncubatorBlockEntity incubator = (IncubatorBlockEntity) te;
                ItemStack itemStack = player.inventory.getItemStack();
                if(ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(itemStack.getItem())) {
                    incubator.placeEgg(message.xPos, message.yPos, itemStack);
                    ((EntityPlayerMP)player).updateHeldItem();
                    incubator.syncToClient();
                }
            }
        }
    }
}
