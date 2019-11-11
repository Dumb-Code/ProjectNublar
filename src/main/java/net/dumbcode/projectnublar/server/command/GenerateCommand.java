package net.dumbcode.projectnublar.server.command;

import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.Structures;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.dumbcode.projectnublar.server.world.structures.structures.StructureTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandlers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
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
        //todo: when the structure system is jsonified, the varibles i have here will be constants that are created at the start of each network
        //todo: These constants can then be references later on - Not that the constant should be a primitive, not just an integer/float
        Random rand = sender.getEntityWorld().rand;

        int color1 = rand.nextInt(16);
        int color2 = rand.nextInt(16);
        while (color1 == color2) {
            color2 = rand.nextInt(16);
        }
        int[] colors = {color1, color2};

        new NetworkBuilder(sender.getEntityWorld(), sender.getPosition())
                .addData(DataHandlers.LOOTTABLE)
                .addData(new DataHandler(DataHandler.Scope.STRUCTURE, s -> s.equals("projectnublar:digsite_wool_1"),
                        (world, pos, name, random) -> Blocks.WOOL.getStateFromMeta(colors[random.nextInt(colors.length)]))
                )

                .generate(rand,
                        BuilderNode.builder(Structure.class)
                        .child(new Digsite(1, 2, 5))
                                .child(new StructureTemplate(Structures.PREBUILT, 1, 1))
                                    .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 3))
                                    .sibling(new StructureTemplate(Structures.CRATE_MEDIUM, 0, 2))
                                    .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 1))
                                    .end()
                                .sibling(new Digsite(3, 2, 0))
                                .sibling(new StructureTemplate(Structures.TENT_LARGE_1, 3, 4))
                                    .child(new StructureTemplate(Structures.CRATE_LARGE, 0, 3))
                                    .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 0, 4))
                                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                        .end()
                                    .sibling(new StructureTemplate(Structures.TENT_SMALL_2, 0, 4))
                                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                        .end()
                                    .sibling(new StructureTemplate(Structures.CRATE_MEDIUM, 0, 4))
                                    .sibling(new StructureTemplate(Structures.CRATE_SMALL, 0, 5))
                                    .end()
                                .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 1, 5))
                                    .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                    .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 3))
                                    .end()
                                .sibling(new StructureTemplate(Structures.TENT_LARGE_2, 4, 5))
                                    .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                    .sibling(new StructureTemplate(Structures.CRATE_LARGE, 0, 3))
                                    .sibling(new StructureTemplate(Structures.TENT_SMALL_1, 0, 4))
                                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                        .end()
                                    .sibling(new StructureTemplate(Structures.TENT_SMALL_2, 0, 4))
                                        .child(new StructureTemplate(Structures.CRATE_SMALL, 0, 1))
                                        .end()

                                .buildToRoots());

    }
}
