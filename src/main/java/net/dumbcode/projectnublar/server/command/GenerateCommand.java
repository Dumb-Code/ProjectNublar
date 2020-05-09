package net.dumbcode.projectnublar.server.command;

import net.dumbcode.projectnublar.server.world.gen.DigsiteStructureNetwork;
import net.dumbcode.projectnublar.server.world.structures.network.StructureNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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




        StructureNetwork.Stats stats = DigsiteStructureNetwork.NETWORK.generate(sender.getEntityWorld(), sender.getPosition(), args.length > 0 ? new Random(args[0].hashCode()) : sender.getEntityWorld().rand);

        sender.sendMessage(new TextComponentString(
            "-------------\n"+
                "total:               " + stats.getStructures() + "\n"+
                "-------------\n"+
                "prepare:          " + stats.getTimeTakenPrepareMs() + "ms\n"+
                "path-generate:  " + stats.getTimeTakenPathGeneration() + "ms\n"+
                "generate:         " + stats.getTimeTakenGenerateMs() + "ms\n"+
                "-------------"
        ));
    }
}
