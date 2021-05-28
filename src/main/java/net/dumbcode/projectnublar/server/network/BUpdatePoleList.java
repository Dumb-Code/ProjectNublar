package net.dumbcode.projectnublar.server.network;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.network.NetworkUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class BUpdatePoleList {

    private final BlockPos pos;
    private final List<SkeletalProperties.Pole> poleList;

    public static BUpdatePoleList fromBytes(PacketBuffer buf) {
        return new BUpdatePoleList(
            buf.readBlockPos(),
            IntStream.range(0, buf.readInt())
                .mapToObj(i -> new SkeletalProperties.Pole(buf.readUtf(), buf.readEnum(PoleFacing.class)))
                .collect(Collectors.toList())
        );
    }

    public static void toBytes(BUpdatePoleList packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.poleList.size());
        for (SkeletalProperties.Pole pole : packet.poleList) {
            buf.writeUtf(pole.getCubeName());
            buf.writeEnum(pole.getFacing());
        }
    }

    public static void handle(BUpdatePoleList message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            World world = NetworkUtils.getPlayer(supplier).getCommandSenderWorld();
            TileEntity blockEntity = world.getBlockEntity(message.pos);
            if (blockEntity instanceof SkeletalBuilderBlockEntity) {
                SkeletalBuilderBlockEntity builder = (SkeletalBuilderBlockEntity)blockEntity;
                builder.getSkeletalProperties().setPoles(message.poleList);
                builder.setChanged();

                if(context.getDirection().getReceptionSide().isServer()) {
                    ProjectNublar.NETWORK.send(PacketDistributor.ALL.noArg(), new BUpdatePoleList(message.pos, message.poleList));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
