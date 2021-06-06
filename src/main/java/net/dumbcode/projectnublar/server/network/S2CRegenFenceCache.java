package net.dumbcode.projectnublar.server.network;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Set;
import java.util.function.Supplier;

public class S2CRegenFenceCache {
    public S2CRegenFenceCache() {
    }

    public static S2CRegenFenceCache fromBytes(PacketBuffer buf) {
        return new S2CRegenFenceCache();
    }


    public static void toBytes(S2CRegenFenceCache packet, PacketBuffer buf) {
    }

    public static void handle(S2CRegenFenceCache packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ClientWorld level = Minecraft.getInstance().level;
            for (TileEntity tileEntity : level.blockEntityList) {
                if(tileEntity instanceof ConnectableBlockEntity) {
                    ConnectableBlockEntity ce = (ConnectableBlockEntity) tileEntity;
                    Set<Connection> newConnection = Sets.newLinkedHashSet();
                    for (Connection con : ce.getConnections()) {
                        newConnection.add(con.copy());
                    }
                    ce.getConnections().clear();
                    ce.getConnections().addAll(newConnection);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
