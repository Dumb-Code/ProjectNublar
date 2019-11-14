package net.dumbcode.projectnublar.server.command;

import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.Structures;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.dumbcode.projectnublar.server.world.structures.structures.StructureTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.HeightDeviationPredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.HeightRangePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.SolidLiquidRatioPredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandlers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Random;

public class GenerateCommand extends CommandBase {

    private static final String N = "nbt_predicates";
    private static final String D = "digsite_predicates";

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
        //todo: when the structure system is jsonified, the varibles i have here will be constants that are created at the start of each network
        //todo: These constants can then be references later on - Not that the constant should be a primitive, not just an integer/float
        Random rand = args.length > 0 ? new Random(args[0].hashCode()) : sender.getEntityWorld().rand;

        int color1 = rand.nextInt(16);
        int color2;
        do {
            color2 = rand.nextInt(16);
        }  while (color1 == color2);
        int[] colors = {color1, color2};

        NetworkBuilder.Stats stats = new NetworkBuilder(sender.getEntityWorld(), sender.getPosition())
            .addData(DataHandlers.LOOTTABLE)
            .addData(new DataHandler(DataHandler.Scope.STRUCTURE, s -> s.equals("projectnublar:digsite_wool_1"),
                    (world, pos, name, random) -> Blocks.WOOL.getStateFromMeta(colors[random.nextInt(colors.length)]))
            )
            .globalPredicate(N,
                new HeightRangePredicate(ValueRange.upperBound(3)),
                new SolidLiquidRatioPredicate(ValueRange.upperBound(0.3D), ValueRange.inifinty())
            )
            .globalPredicate(D,
                new HeightRangePredicate(ValueRange.upperBound(6)),
                new SolidLiquidRatioPredicate(ValueRange.upperBound(0.3D), ValueRange.inifinty())
            )
            .generate(rand,
                BuilderNode.builder(Structure.class)
                .child(new Digsite(1, 2, 10).addGlobalPredicates(D))
                    .child(new StructureTemplate(Structures.PREBUILT, 1, 1).fs().addGlobalPredicates(N))
                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 3).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.CRATE_MEDIUM, 0, 2).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 1).fs().addGlobalPredicates(N))
                        .end()
                    .sibling(new Digsite(3, 2, 0).addGlobalPredicates(D))
                    .sibling(new StructureTemplate(Structures.TENT_LARGE_1, 5, 4).fs().addGlobalPredicates(N))
                        .child(new StructureTemplate(Structures.CRATE_LARGE, 0, 3).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 0, 4).fs().addGlobalPredicates(N))
                            .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                            .end()
                        .sibling(new StructureTemplate(Structures.TENT_SMALL_2, 2, 4).fs().addGlobalPredicates(N))
                            .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                            .end()
                        .sibling(new StructureTemplate(Structures.CRATE_MEDIUM, 0, 4).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.CRATE_SMALL, 0, 5).fs().addGlobalPredicates(N))
                        .end()
                    .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 1, 5).fs().addGlobalPredicates(N))
                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 3).fs().addGlobalPredicates(N))
                        .end()
                    .sibling(new StructureTemplate(Structures.TENT_LARGE_2, 4, 5).fs().addGlobalPredicates(N))
                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 3).fs().addGlobalPredicates(N))
                        .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 2, 4).fs().addGlobalPredicates(N))
                            .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                            .end()
                        .sibling(new StructureTemplate(Structures.TENT_SMALL_2, 1, 4).fs().addGlobalPredicates(N))
                            .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1).fs().addGlobalPredicates(N))
                            .end()
                    .buildToRoots());

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
