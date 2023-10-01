package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.projectnublar.server.world.gen.DigsiteStructureNetwork;
import net.dumbcode.projectnublar.server.world.structures.network.StructureNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.Random;

public class GenerateCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("generate")
            .executes(context -> generate(context.getSource(), context.getSource().getLevel().random))
            .then(Commands.argument("seed", StringArgumentType.word())
                .executes(context -> generate(context.getSource(), new Random(context.getArgument("seed", String.class).hashCode())))
            );
    }

    private static int generate(CommandSource source, Random random) {
        StructureNetwork.Stats stats = DigsiteStructureNetwork.NETWORK.generate(source.getLevel(), new BlockPos(source.getPosition()), random);

        source.sendSuccess(Component.literal(
            "-------------\n"+
                "total:               " + stats.getStructures() + "\n"+
                "-------------\n"+
                "prepare:          " + stats.getTimeTakenPrepareMs() + "ms\n"+
                "path-generate:  " + stats.getTimeTakenPathGeneration() + "ms\n"+
                "generate:         " + stats.getTimeTakenGenerateMs() + "ms\n"+
                "-------------"
        ), false);

        return 1;
    }

}
