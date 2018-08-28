package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.client.gui.GuiSkeletalProperties;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S12ChangePoleFacing implements IMessage {

    private int x;
    private int y;
    private int z;
    private PoleFacing newFacing;

    public S12ChangePoleFacing() { }

    public S12ChangePoleFacing(SkeletalBuilderBlockEntity builder, PoleFacing newFacing) {
        this.x = builder.getPos().getX();
        this.y = builder.getPos().getY();
        this.z = builder.getPos().getZ();
        this.newFacing = newFacing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        newFacing = PoleFacing.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(newFacing.ordinal());
    }

    public static class Handler extends WorldModificationsMessageHandler<S12ChangePoleFacing, IMessage> {
        @Override
        protected void handleMessage(S12ChangePoleFacing message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = player.world.getTileEntity(pos);
            if(te instanceof SkeletalBuilderBlockEntity) {
                ((SkeletalBuilderBlockEntity)te).getSkeletalProperties().setPoleFacing(message.newFacing);
            }
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if(screen instanceof GuiSkeletalProperties) {
                GuiSkeletalProperties gui = (GuiSkeletalProperties)screen;
                if(gui.getBuilder().getPos().equals(pos)) {
                    gui.setPoleFacing(message.newFacing);//Update the gui screen for other players
                }
            }
            pos.release();
        }
    }
}
