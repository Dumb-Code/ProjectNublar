package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.HerdSavedData;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.Set;

public class FinishProcessCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("finishprocess")
            .executes(context -> {
                ServerWorld level = context.getSource().getLevel();
                for (TileEntity entity : level.blockEntityList) {
                    if(entity instanceof MachineModuleBlockEntity) {
                        MachineModuleBlockEntity<?> be = (MachineModuleBlockEntity<?>) entity;
                        for (int i = 0; i < be.getProcessCount(); i++) {
                            MachineModuleBlockEntity.MachineProcess<?> process = be.getProcess(i);
                            if(process.isProcessing()) {
                                process.setTime(process.getTotalTime());
                            }
                        }
                    }
                }
                return 1;
            });
    }
}
