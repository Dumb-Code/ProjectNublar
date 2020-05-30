package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class S44SyncOpenedUsers implements IMessage {

    private BlockPos pos;
    private final Set<UUID> uuidSet = new HashSet<>();

    public S44SyncOpenedUsers() {
    }

    public S44SyncOpenedUsers(MachineModuleBlockEntity<?> machine) {
        this.pos = machine.getPos();
        this.uuidSet.addAll(machine.getOpenedUsers());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.uuidSet.add(new UUID(buf.readLong(), buf.readLong()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeShort(this.uuidSet.size());
        for (UUID uuid : this.uuidSet) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
        }
    }

    public static class Handler extends WorldModificationsMessageHandler<S44SyncOpenedUsers, S44SyncOpenedUsers> {

        @Override
        protected void handleMessage(S44SyncOpenedUsers message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity entity = world.getTileEntity(message.pos);
            if(entity instanceof MachineModuleBlockEntity) {
                Set<UUID> users = ((MachineModuleBlockEntity<?>) entity).getOpenedUsers();
                users.clear();
                users.addAll(message.uuidSet);
            }
        }
    }
}
