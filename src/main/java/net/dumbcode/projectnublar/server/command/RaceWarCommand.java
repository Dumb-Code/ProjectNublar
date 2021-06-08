package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.HerdSavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.Set;

public class RaceWarCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("racewar")
            .executes(context -> {
                ServerWorld level = context.getSource().getLevel();
                Set<HerdSavedData> dataSet = new HashSet<>();
                for (Entity entity : level.getAllEntities()) {
                    if(entity instanceof ComponentAccess) {
                        ((ComponentAccess) entity).get(EntityComponentTypes.HERD).flatMap(HerdComponent::getHerdData).ifPresent(dataSet::add);
                    }
                }

                context.getSource().sendSuccess(new StringTextComponent("Size: " + dataSet.size()), false);

                for (HerdSavedData data : dataSet) {
                    for (HerdSavedData other : dataSet) {
                        if(data == other) {
                            continue;
                        }

                        other.getMembers().forEach(data::addEnemy);
                    }
                }
                return 1;
            });
    }
}
