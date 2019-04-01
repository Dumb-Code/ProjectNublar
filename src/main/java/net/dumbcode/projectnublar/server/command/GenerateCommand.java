package net.dumbcode.projectnublar.server.command;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.dumbcode.projectnublar.server.world.structures.structures.Digsite;
import net.dumbcode.projectnublar.server.world.structures.structures.StructureTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

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
                        .child(new Digsite(1, 4))
                                .child(new Digsite(3, 1))
                                .sibling(new StructureTemplate(NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/greentent_large")), 0, 50))
                                    .child(new StructureTemplate(NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_large")), 0, 3))
                                    .sibling(new StructureTemplate(NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_medium")), 0, 4))
                                    .sibling(new StructureTemplate(NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/crate_small")), 0, 5))
                                    .end()
                                .sibling(new StructureTemplate(NBTTemplate.readFromFile(new ResourceLocation(ProjectNublar.MODID, "digsite/greentent_small")), 0, 6))

                                .buildToRoots());

    }
}
