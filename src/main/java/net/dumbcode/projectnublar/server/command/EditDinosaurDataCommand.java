package net.dumbcode.projectnublar.server.command;

import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.BreedingComponent;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import scala.actors.$bang;

import java.util.*;
import java.util.function.*;

public class EditDinosaurDataCommand extends CommandBase {

    private final Map<String, ChangeableEntry<?>> entries = new HashMap<>();

    public EditDinosaurDataCommand() {
        this.entries.put("thirst", new ChangeableEntry<>(ComponentHandler.METABOLISM, MetabolismComponent::getWater, MetabolismComponent::setWater));
        this.entries.put("hunger", new ChangeableEntry<>(ComponentHandler.METABOLISM, MetabolismComponent::getFood, MetabolismComponent::setFood));
        this.entries.put("breed", new ChangeableEntry<>(EntityComponentTypes.BREEDING, BreedingComponent::getTicksSinceLastBreed, BreedingComponent::setTicksSinceLastBreed));
        this.entries.put("pregnancy", new ChangeableEntry<>(ComponentHandler.DINOSAUR_EGG_LAYING, e -> -1, (c, i) -> c.getHeldEggs().forEach(e -> e.setTicksLeft(i))));
    }

    @Override
    public String getName() {
        return "ticks";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "todo";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length >= 1) {
            ChangeableEntry<?> entry = this.entries.get(args[0]);
            List<ComponentAccess> accessList = new ArrayList<>();
            for (Entity entity : sender.getEntityWorld().loadedEntityList) {
                if(entity instanceof ComponentAccess) {
                    accessList.add((ComponentAccess) entity);
                }
            }

            if(entry == null) {
                throw new CommandException(args[0] + " is not valid. Please use: " + this.entries.keySet().toString());
            }
            if(args.length >= 2) {
                //set
                int ticksSet = parseInt(args[1]);
                int affected = 0;
                for (ComponentAccess access : accessList) {
                    if (runSetters(access, entry, ticksSet)) {
                        affected++;
                    }
                }
                sender.sendMessage(new TextComponentString("Affected: " + affected));

            } else {
                //get
                List<Double> out = new ArrayList<>();
                for (ComponentAccess access : accessList) {
                    runGetters(access, entry).ifPresent(out::add);
                }

                sender.sendMessage(new TextComponentString("Command Output: " + out));
            }
        }
    }

    private <E extends EntityComponent> Optional<Double> runGetters(ComponentAccess access, ChangeableEntry<E> entry) {
        return access.get(entry.getType()).map(e -> entry.getValueGetter().applyAsDouble(e));
    }

    private <E extends EntityComponent> boolean runSetters(ComponentAccess access, ChangeableEntry<E> entry, int value) {
        return access.get(entry.getType()).map(e -> {
            entry.getValueSetter().accept(e, value);
            e.syncToClient();
            return true;
        }).orElse(false);
    }

    @Value
    private static class ChangeableEntry<E extends EntityComponent> {
        EntityComponentType<E, ?> type;
        ToDoubleFunction<E> valueGetter;
        BiConsumer<E, Integer> valueSetter;
    }
}
