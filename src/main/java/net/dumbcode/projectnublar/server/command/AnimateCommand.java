package net.dumbcode.projectnublar.server.command;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AnimationComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class AnimateCommand extends CommandBase {
    @Override
    public String getName() {
        return "animate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "usage here";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EnumAnimation animation = EnumAnimation.valueOf(args[0].toUpperCase(Locale.ROOT));
        for (Entity entity : sender.getEntityWorld().loadedEntityList) {
            if(entity instanceof DinosaurEntity) {
                AnimationComponent comp = ((DinosaurEntity) entity).getOrNull(EntityComponentTypes.ANIMATION);
                if (comp != null) {
                    comp.setAnimation((DinosaurEntity) entity, animation.get());
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, EnumAnimation.getNames()) : Lists.newArrayList();
    }
}
