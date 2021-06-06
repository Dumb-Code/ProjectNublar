package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandProjectNublar {

//    public CommandProjectNublar() {
//        this.addSubcommand(new RegenFenceCacheCommand());
//        this.addSubcommand(new GenerateCommand());
//        this.addSubcommand(new EditDinosaurDataCommand());
//        this.addSubcommand(new RaceWarCommand());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("projectnublar")
            .then(SpawnDinosaurCommand.createCommand())
            .then(AnimateCommand.createCommand())
        );
    }
}
