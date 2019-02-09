package net.dumbcode.projectnublar.server.command;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.network.S20RegenCache;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import java.util.Set;

public class RegenFenceCacheCommand extends CommandBase {
    @Override
    public String getName() {
        return "regenfence";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return ""; //TODO:
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        for (WorldServer world : server.worlds) {
            for (TileEntity te : world.loadedTileEntityList) {
                if(te instanceof ConnectableBlockEntity) {
                    ConnectableBlockEntity ce = (ConnectableBlockEntity) te;
                    Set<Connection> newConnection = Sets.newLinkedHashSet();
                    for (Connection con : ce.getConnections()) {
                        newConnection.add(new Connection(sender.getEntityWorld(), con.getType(), con.getOffset(), con.getFrom(), con.getTo(), con.getNext(), con.getPrevious(), con.getPosition()));
                    }
                    ce.getConnections().clear();
                    ce.getConnections().addAll(newConnection);
                }
            }
        }
        ProjectNublar.NETWORK.sendToAll(new S20RegenCache());
    }
}
