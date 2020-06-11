package net.dumbcode.projectnublar.server.command;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.HerdSavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.HashSet;
import java.util.Set;

public class RaceWarCommand extends CommandBase {
    @Override
    public String getName() {
        return "racewar";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "todo";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Set<HerdSavedData> dataSet = new HashSet<>();
        for (Entity entity : sender.getEntityWorld().loadedEntityList) {
            if(entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).get(EntityComponentTypes.HERD).flatMap(HerdComponent::getHerdData).ifPresent(dataSet::add);
            }
        }

        sender.sendMessage(new TextComponentString("Size: " + dataSet.size()));

        for (HerdSavedData data : dataSet) {
            for (HerdSavedData other : dataSet) {
                if(data == other) {
                    continue;
                }

                other.getMembers().forEach(data::addEnemy);
            }
        }
    }
}
