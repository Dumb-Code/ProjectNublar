package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S21SpawnParticle implements IMessage {

    private ParticleType particleType;
    private double xPos;
    private double yPos;
    private double zPos;
    private double xMotion;
    private double yMotion;
    private double zMotion;
    private int amount;
    private int[] data;

    public S21SpawnParticle() {
    }

    public S21SpawnParticle(ParticleType type, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int amount, int[] data) {
        this.particleType = type;
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.xMotion = xMotion;
        this.yMotion = yMotion;
        this.zMotion = zMotion;
        this.amount = amount;
        this.data = data;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        this.particleType = ParticleType.values()[buf.readInt()];
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.xMotion = buf.readDouble();
        this.yMotion = buf.readDouble();
        this.zMotion = buf.readDouble();
        this.amount = buf.readInt();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.data[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.particleType.ordinal());
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeDouble(this.xMotion);
        buf.writeDouble(this.yMotion);
        buf.writeDouble(this.zMotion);
        buf.writeInt(this.amount);
        buf.writeInt(this.data.length);
        for (int datum : this.data) {
            buf.writeInt(datum);
        }
    }

    public static class Handler extends WorldModificationsMessageHandler<S21SpawnParticle, S21SpawnParticle> {

        @Override
        protected void handleMessage(S21SpawnParticle message, MessageContext ctx, World world, EntityPlayer player) {
            ProjectNublar.spawnParticles(message.particleType, world, message.xPos, message.yPos, message.zPos, message.xMotion, message.yMotion, message.zMotion, message.amount, message.data);
        }
    }
}
