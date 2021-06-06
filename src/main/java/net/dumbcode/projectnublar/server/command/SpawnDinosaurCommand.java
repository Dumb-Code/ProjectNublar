package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.command.EnumArgument;

import javax.annotation.Nullable;

public class SpawnDinosaurCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("spawn").then(
            Commands.argument("dinosaur", new DinosaurArgument())
                .executes(context -> spawn(context.getSource(), context.getArgument("dinosaur", Dinosaur.class), context.getSource().getPosition(), null))
            .then(
                Commands.argument("pos", Vec3Argument.vec3())
                    .executes(context -> spawn(context.getSource(), context.getArgument("dinosaur", Dinosaur.class), context.getArgument("pos", Vector3d.class), null))
                .then(
                    Commands.argument("gender", EnumArgument.enumArgument(DinosaurGender.class))
                    .executes(context -> spawn(context.getSource(), context.getArgument("dinosaur", Dinosaur.class), context.getArgument("pos", Vector3d.class), context.getArgument("gender", DinosaurGender.class)))
                )
            )
        );
    }

    private static int spawn(CommandSource source, Dinosaur dinosaur, Vector3d position, @Nullable DinosaurGender gender) {
        ServerWorld level = source.getLevel();
        DinosaurEntity entity = dinosaur.createEntity(level);
        if(gender != null) {
            entity.get(EntityComponentTypes.GENDER).ifPresent(g -> g.male = gender == DinosaurGender.male);
        }
        entity.setPos(position.x, position.y, position.z);
        level.addFreshEntity(entity);
        return 1;
    }

    private enum DinosaurGender {
        male, female
    }
}
