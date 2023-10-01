package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.network.NetworkUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class BChangeGlobalRotation {

    private final BlockPos pos;
    private final float newRotation;


    public static BChangeGlobalRotation fromBytes(PacketBuffer buf) {
        return new BChangeGlobalRotation(buf.readBlockPos(), buf.readFloat());
    }

    public static void toBytes(BChangeGlobalRotation packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.newRotation);
    }

    public static void handle(BChangeGlobalRotation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            TileEntity blockEntity = world.getBlockEntity(message.pos);
            if (blockEntity instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)blockEntity;
                builder.getSkeletalProperties().setRotation(message.newRotation);
                builder.setChanged();

                if(context.getDirection().getReceptionSide().isServer()) {
                    ProjectNublar.NETWORK.send(PacketDistributor.ALL.noArg(), new BChangeGlobalRotation(message.pos, message.newRotation));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
