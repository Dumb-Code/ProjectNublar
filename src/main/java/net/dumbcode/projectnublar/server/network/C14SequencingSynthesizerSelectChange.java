package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C14SequencingSynthesizerSelectChange implements IMessage {

    private BlockPos position;
    private int id;
    private String key;
    private double amount;

    @SuppressWarnings("unused")
    public C14SequencingSynthesizerSelectChange() {
    }

    public C14SequencingSynthesizerSelectChange(BlockPos position, int id, String key, double amount) {
        this.position = position;
        this.id = id;
        this.key = key;
        this.amount = amount;
    }



    @Override
    public void fromBytes(ByteBuf buf) {
        this.position = BlockPos.fromLong(buf.readLong());
        this.id = buf.readInt();
        this.key = ByteBufUtils.readUTF8String(buf);
        this.amount = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.position.toLong());
        buf.writeInt(this.id);
        ByteBufUtils.writeUTF8String(buf, this.key);
        buf.writeDouble(this.amount);
    }

    public static class Handler extends WorldModificationsMessageHandler<C14SequencingSynthesizerSelectChange, IMessage> {

        @Override
        protected void handleMessage(C14SequencingSynthesizerSelectChange message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity tileEntity = world.getTileEntity(message.position);
            if(tileEntity instanceof SequencingSynthesizerBlockEntity) {
                SequencingSynthesizerBlockEntity seq = (SequencingSynthesizerBlockEntity) tileEntity;
                //TODO: validation
                seq.setSelect(message.id, message.key, message.amount);
                ProjectNublar.NETWORK.sendToDimension(new S15SyncSequencingSynthesizerSelectChange(message.position, message.id, message.key, message.amount), world.provider.getDimension());
            }
        }
    }
}
