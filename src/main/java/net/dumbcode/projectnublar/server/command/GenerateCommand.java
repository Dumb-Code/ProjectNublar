package net.dumbcode.projectnublar.server.command;

import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
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
                                        .child(new Structure("test", 5, 3, 1, 12))
                                            .child(new Structure("test", 5, 6, 12, 1))
                                            .sibling(new Structure("test", 2, 1, 6, 1))
                                            .end()
                                        .sibling(new Structure("test", 5, 3, 1, 12))
                                            .child(new Structure("test", 2, 6, 12, 1))
                                            .sibling(new Structure("test", 4, 2, 6, 1))
                                            .sibling(new Structure("test", 1, 5, 6, 1))
                                            .sibling(new Structure("test", 2, 3, 6, 1))
                                            .end()
                                        .sibling(new Structure("test", 5, 3, 20, 4))
                                            .child(new Structure("test", 2, 6, 12, 1))
                                            .sibling(new Structure("test", 4, 2, 12, 1))
                                                .child(new Structure("", 12, 22, 15, 2))
                                                .end()
                                            .sibling(new Structure("test", 2, 3, 6, 1))
                                            .end()
                                        .sibling(new Structure("test", 5, 3, 1, 3))
                                            .child(new Structure("test", 2, 6, 12, 1))
                                            .sibling(new Structure("test", 2, 5, 6, 1))
                                            .end()
                                .buildToRoots());
    }
}
