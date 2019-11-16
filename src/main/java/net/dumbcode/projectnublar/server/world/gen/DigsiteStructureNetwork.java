package net.dumbcode.projectnublar.server.world.gen;

import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.dumbcode.projectnublar.server.world.constants.ConstantDefinition;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.Structures;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.network.StructureNetwork;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.dumbcode.projectnublar.server.world.structures.structures.StructureTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.HeightRangePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.SolidLiquidRatioPredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandlers;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class DigsiteStructureNetwork {
    private static final ConstantDefinition<IBlockState> WOOL_1 = new ConstantDefinition<>();
    private static final ConstantDefinition<IBlockState> WOOL_2 = new ConstantDefinition<>();


    private static final String N = "nbt_predicates";
    private static final String D = "digsite_predicates";


    public static final StructureNetwork NETWORK = new NetworkBuilder()
    .addData(DataHandlers.LOOTTABLE)
    .addData(new DataHandler(DataHandler.Scope.STRUCTURE, s -> s.equals("projectnublar:digsite_wool_1"),
        (world, pos, name, random, decision) -> decision.requireEntry((random.nextBoolean() ? WOOL_1 : WOOL_2)))
    )

    .addConstant(WOOL_1, random -> Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byMetadata(random.nextInt(16))))
    .addConstant(WOOL_2, random -> Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byDyeDamage(random.nextInt(16))))

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

}
