package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.entity.vehicles.AbstractVehicle;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class C2SVehicleInputStateUpdated {

    private final int entityInt;
    private final int state;

    public static C2SVehicleInputStateUpdated fromBytes(PacketBuffer buf) {
        return new C2SVehicleInputStateUpdated(buf.readInt(), buf.readInt());
    }

    public static void toBytes(C2SVehicleInputStateUpdated packet, PacketBuffer buf) {
        buf.writeInt(packet.entityInt);
        buf.writeInt(packet.state);
    }

    public static void handle(C2SVehicleInputStateUpdated message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = context.getSender().level.getEntity(message.entityInt);
            if(entity instanceof AbstractVehicle) {
                ((AbstractVehicle<?>)entity).setControlState(message.state);
            }
        });
        context.setPacketHandled(true);
    }
}
