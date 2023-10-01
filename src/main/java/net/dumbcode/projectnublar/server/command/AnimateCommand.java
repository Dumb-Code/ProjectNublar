package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class AnimateCommand {

    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("animation")
            .then(Commands.literal("play").then(
                Commands.argument("animation_id", ResourceLocationArgument.id())
                    .executes(context -> animate(context.getSource(), new Animation(context.getArgument("animation_id", ResourceLocation.class)), null))
                    .then(Commands.argument("channel", IntegerArgumentType.integer(0))
                        .executes(context -> animate(context.getSource(), new Animation(context.getArgument("animation_id", ResourceLocation.class)), context.getArgument("channel", Integer.class)))
                    )
            ))
            .then(Commands.literal("stop").executes(context -> stopAll(context.getSource())));
    }

    private static int stopAll(CommandSource source) {
        for (AnimationComponent component : gatherAll(source.getLevel())) {
            component.stopAll();
        }
        return 1;
    }

    private static int animate(CommandSource source, Animation animation, @Nullable Integer channel) {
        for (AnimationComponent component : gatherAll(source.getLevel())) {
            component.playAnimation(animation, channel == null ? 0 : channel);
        }
        return 1;
    }

    private static Iterable<AnimationComponent> gatherAll(ServerWorld world) {
        return () -> StreamSupport.stream(world.getAllEntities().spliterator(), false)
            .filter(e -> e instanceof DinosaurEntity)
            .map(e -> ((DinosaurEntity) e).get(EntityComponentTypes.ANIMATION))
            .filter(Optional::isPresent).map(Optional::get)
            .iterator();
    }
}
