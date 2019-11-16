package net.dumbcode.projectnublar.server.command;

import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.dumbcode.projectnublar.server.world.constants.ConstantDefinition;
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

        ConstantDefinition<IBlockState> wool1 = new ConstantDefinition<>();
        ConstantDefinition<IBlockState> wool2 = new ConstantDefinition<>();

        StructureNetwork network = new NetworkBuilder()

            .addData(DataHandlers.LOOTTABLE)
            .addData(new DataHandler(DataHandler.Scope.STRUCTURE, s -> s.equals("projectnublar:digsite_wool_1"),
                (world, pos, name, random, decision) -> decision.requireEntry((random.nextBoolean() ? wool1 : wool2)))
            )

            .addConstant(wool1, random -> Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byMetadata(random.nextInt(16))))
            .addConstant(wool2, random -> Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byDyeDamage(random.nextInt(16))))

            .globalPredicate(N,
                new HeightRangePredicate(ValueRange.upperBound(3)),
                new SolidLiquidRatioPredicate(ValueRange.upperBound(0.3D), ValueRange.inifinty())
            )
            .globalPredicate(D,
                new HeightRangePredicate(ValueRange.upperBound(6)),
                new SolidLiquidRatioPredicate(ValueRange.upperBound(0.3D), ValueRange.inifinty())
            )
            .build(BuilderNode.builder(Structure.class)
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
                    .buildToRoots()
            );

        StructureNetwork.Stats stats = network.generate(sender.getEntityWorld(), sender.getPosition(), args.length > 0 ? new Random(args[0].hashCode()) : sender.getEntityWorld().rand);

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
