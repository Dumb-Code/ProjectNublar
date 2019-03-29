package net.dumbcode.projectnublar.server.command;

import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Random;

public class GenerateCommand extends CommandBase {
    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "todo";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        new NetworkBuilder(sender.getEntityWorld(), sender.getPosition())
                .generate(new Random(),
                        BuilderNode.builder(Structure.class)

                        .child(new Digsite(2, 3))
                                .child(new Digsite(2, 1))

                        .buildToRoots());

    }
}
