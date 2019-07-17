package net.dumbcode.projectnublar.server.command;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.animation.interpolation.CosInterpolation;
import net.dumbcode.dumblibrary.server.animation.interpolation.LinearInterpolation;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

public class AnimateCommand extends CommandBase {
    @Override
    public String getName() {
        return "animate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /projectnublar animate {animation}";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length > 0) {
            Animation animation = DumbRegistries.ANIMATION_REGISTRY.getValue(new ResourceLocation(args[0]));
            for (Entity entity : sender.getEntityWorld().loadedEntityList) {
                if (entity instanceof DinosaurEntity) {
                    AnimationComponent comp = ((DinosaurEntity) entity).getOrNull(EntityComponentTypes.ANIMATION);
                    if (comp != null) {
                        AnimationLayer.AnimationEntry entry = new AnimationLayer.AnimationEntry(animation);

                        if (args.length > 1) {
                            if(args[1].equalsIgnoreCase("stop")) {
                                comp.stopAll();
                            }
                            entry = entry.withTime(parseInt(args[1]));
                        }
                        if (args.length > 2) {
                            entry = entry.withHold(parseBoolean(args[2]));
                        }
                        if (args.length > 3) {
                            entry = entry.withUseInertia(parseBoolean(args[3]));
                        }
                        if (args.length > 4) {
                            int interp = parseInt(args[4]);
                            if (interp == 1) {
                                entry = entry.withInterpolation(new CosInterpolation()); // TODO: Fix this not working.
                            } else {
                                entry = entry.withInterpolation(new LinearInterpolation());
                            }
                        }
                        comp.playAnimation((ComponentAccess) entity, entry, args.length > 5 ? parseInt(args[5]) : 0); //ATTEMPT MERGE
                    }
                }
            }
            return;
        }
        TextComponentTranslation text = new TextComponentTranslation("Usage: /projectnublar animate {animation}");
        text.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(text);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, DumbRegistries.ANIMATION_REGISTRY.getKeys()) : Lists.newArrayList();
    }
}
