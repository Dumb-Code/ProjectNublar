package net.dumbcode.projectnublar.server.command;

import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.dumbcode.projectnublar.server.world.constants.ConstantDefinition;
import net.dumbcode.projectnublar.server.world.gen.DigsiteStructureNetwork;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.Structures;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.network.StructureNetwork;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.dumbcode.projectnublar.server.world.structures.structures.StructureTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.HeightDeviationPredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.HeightRangePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.SolidLiquidRatioPredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandlers;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Random;
import java.util.function.Function;

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
