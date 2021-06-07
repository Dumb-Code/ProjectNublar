package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.graalvm.compiler.asm.sparc.SPARCMacroAssembler;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class S2CSyncSequencingSynthesizerSelectChange {

    private final BlockPos pos;
    private final int id;
    private final String key;
    private final double amount;

    public static S2CSyncSequencingSynthesizerSelectChange fromBytes(PacketBuffer buf) {
        return new S2CSyncSequencingSynthesizerSelectChange(
            buf.readBlockPos(), buf.readInt(), buf.readUtf(), buf.readDouble()
        );
    }

    public static void toBytes(S2CSyncSequencingSynthesizerSelectChange packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.id);
        buf.writeUtf(packet.key);
        buf.writeDouble(packet.amount);
    }

    public static void handle(S2CSyncSequencingSynthesizerSelectChange message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            TileEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos);
            if(blockEntity instanceof SequencingSynthesizerBlockEntity) {
                ((SequencingSynthesizerBlockEntity) blockEntity).setSelect(message.id, message.key, message.amount);
            }
        });
        context.setPacketHandled(true);
    }

}
