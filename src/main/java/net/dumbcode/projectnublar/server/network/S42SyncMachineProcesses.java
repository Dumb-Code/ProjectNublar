package net.dumbcode.projectnublar.server.network;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class S42SyncMachineProcesses implements IMessage {

    private BlockPos pos;
    private final List<ProcessSync> processList = new ArrayList<>();

    public S42SyncMachineProcesses() {
    }

    public S42SyncMachineProcesses(MachineModuleBlockEntity<?> entity) {
        this.pos = entity.getPos();
        for (int i = 0; i < entity.getProcessCount(); i++) {
            MachineModuleBlockEntity.MachineProcess<?> process = entity.getProcess(i);
            this.processList.add(new ProcessSync(process.getTime(), process.getTotalTime(), process.isProcessing(), process.isHasPower()));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        byte size = buf.readByte();
        for (int i = 0; i < size; i++) {
            this.processList.add(new ProcessSync(buf.readShort(), buf.readShort(), buf.readBoolean(), buf.readBoolean()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeByte(this.processList.size());
        for (ProcessSync sync : this.processList) {
            buf.writeShort(sync.getTime());
            buf.writeShort(sync.getTotalTime());
            buf.writeBoolean(sync.isProcessing());
            buf.writeBoolean(sync.isHasPower());
        }
    }

    @Value private static class ProcessSync { int time, totalTime; boolean processing, hasPower; }

    public static class Handler extends WorldModificationsMessageHandler<S42SyncMachineProcesses, S42SyncMachineProcesses> {

        @Override
        protected void handleMessage(S42SyncMachineProcesses message, MessageContext ctx, World world, EntityPlayer player) {
            TileEntity te = world.getTileEntity(message.pos);
            if(te instanceof MachineModuleBlockEntity) {
                MachineModuleBlockEntity<?> entity = (MachineModuleBlockEntity<?>) te;
                for (int i = 0; i < message.processList.size(); i++) {
                    MachineModuleBlockEntity.MachineProcess<?> process = entity.getProcess(i);
                    ProcessSync sync = message.processList.get(i);
                    process.setTime(sync.getTime());
                    process.setTotalTime(sync.getTotalTime());
                    process.setProcessing(sync.processing && sync.hasPower);
                    process.setHasPower(sync.hasPower);
                }
            }

        }
    }
}
