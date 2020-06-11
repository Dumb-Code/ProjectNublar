package net.dumbcode.projectnublar.server.command;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandProjectNublar extends CommandTreeBase {

    public CommandProjectNublar() {
        this.addSubcommand(new SpawnDinosaurCommand());
        this.addSubcommand(new AnimateCommand());
        this.addSubcommand(new RegenFenceCacheCommand());
        this.addSubcommand(new GenerateCommand());
        this.addSubcommand(new EditDinosaurDataCommand());
    }


    @Override
    public String getName() {
        return "projectnublar";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "this needs to be done";
    }
}
