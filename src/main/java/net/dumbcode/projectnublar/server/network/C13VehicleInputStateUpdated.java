package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.entity.vehicles.AbstractVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C13VehicleInputStateUpdated implements IMessage {

    private int entityInt;
    private int state;

    public C13VehicleInputStateUpdated() {}

    public C13VehicleInputStateUpdated(AbstractVehicle vehicle, int newState) {
        this.entityInt = vehicle.getEntityId();
        this.state = newState;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityInt = buf.readInt();
        this.state = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityInt);
        buf.writeInt(this.state);
    }

    public static class Handler extends WorldModificationsMessageHandler<C13VehicleInputStateUpdated, IMessage> {
        @Override
        protected void handleMessage(C13VehicleInputStateUpdated message, MessageContext ctx, World world, EntityPlayer player) {
            Entity entity = world.getEntityByID(message.entityInt);
            if(entity instanceof AbstractVehicle) {
                ((AbstractVehicle)entity).setControlState(message.state);
            }
        }
    }
}
