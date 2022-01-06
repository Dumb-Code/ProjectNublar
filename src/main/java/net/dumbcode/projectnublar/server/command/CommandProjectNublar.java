package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandProjectNublar {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("projectnublar")
                .requires(source -> source.hasPermission(2))
                .then(SpawnDinosaurCommand.createCommand())
                .then(AnimateCommand.createCommand())
                .then(RegenFenceCacheCommand.createCommand())
                .then(GenerateCommand.createCommand())
                .then(EditDinosaurDataCommand.createCommand())
                .then(RaceWarCommand.createCommand())
                .then(FinishProcessCommand.createCommand())
        );
    }
}
