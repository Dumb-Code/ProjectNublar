package net.dumbcode.projectnublar.server.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.network.S2CRegenFenceCache;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Set;

public class RegenFenceCacheCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("regenfence")
            .executes(context -> {
                for (RegistryKey<World> level : context.getSource().levels()) {
                    ServerWorld world = context.getSource().getServer().getLevel(level);
                    for (TileEntity tileEntity : world.blockEntityList) {
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
                }
                ProjectNublar.NETWORK.send(PacketDistributor.ALL.noArg(), new S2CRegenFenceCache());
                return 1;
            });
    }
}
