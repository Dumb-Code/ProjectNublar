package net.dumbcode.projectnublar.server.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.BreedingComponent;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurEggLayingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

public class EditDinosaurDataCommand {

    public static final ChangeableEntry<MetabolismComponent> THIRST = new ChangeableEntry<>("thirst", ComponentHandler.METABOLISM, MetabolismComponent::getWater, MetabolismComponent::setWater);
    public static final ChangeableEntry<MetabolismComponent> HUNGER = new ChangeableEntry<>("hunger", ComponentHandler.METABOLISM, MetabolismComponent::getFood, MetabolismComponent::setFood);
    public static final ChangeableEntry<BreedingComponent> BREED = new ChangeableEntry<>("breed", EntityComponentTypes.BREEDING, BreedingComponent::getTicksSinceLastBreed, BreedingComponent::setTicksSinceLastBreed);
    public static final ChangeableEntry<DinosaurEggLayingComponent> PREGNANCY = new ChangeableEntry<>("pregnancy", ComponentHandler.DINOSAUR_EGG_LAYING, e -> -1, (c, i) -> c.getHeldEggs().forEach(e -> e.setTicksLeft(i)));


    public static ArgumentBuilder<CommandSource, ?> createCommand() {
        return Commands.literal("ticks")
            .then(createEntry(THIRST))
            .then(createEntry(HUNGER))
            .then(createEntry(BREED))
            .then(createEntry(PREGNANCY))
            ;
    }

    private static <E extends EntityComponent> ArgumentBuilder<CommandSource, ?> createEntry(ChangeableEntry<E> entry) {
        return Commands.literal(entry.name)
            .then(Commands.argument("targets", EntityArgument.entities()))
            .executes(context -> {
                List<Double> out = new ArrayList<>();
                for (ComponentAccess access : gather(context)) {
                    runGetters(access, entry).ifPresent(out::add);
                }
                context.getSource().sendSuccess(new StringTextComponent("Command Output: " + out), false);
                return 1;
            })
            .then(Commands.argument("value", IntegerArgumentType.integer())
            .executes(context -> {
                Integer value = context.getArgument("value", Integer.class);
                int affected = 0;
                for (ComponentAccess access : gather(context)) {
                    if (runSetters(access, entry, value)) {
                        affected++;
                    }
                }
                context.getSource().sendSuccess(new StringTextComponent("Affected: " + affected), false);
                return 1;
            }));
    }

    private static List<ComponentAccess> gather(CommandContext<CommandSource> context) throws CommandSyntaxException {
        EntitySelector target = context.getArgument("targets", EntitySelector.class);
        CommandSource source = context.getSource();
        List<? extends Entity> entities = target.findEntities(source);
        List<ComponentAccess> accessList = new ArrayList<>();
        for (Entity entity : entities) {
            if(entity instanceof ComponentAccess) {
                accessList.add((ComponentAccess) entity);
            }
        }
        return accessList;
    }

    private static <E extends EntityComponent> Optional<Double> runGetters(ComponentAccess access, ChangeableEntry<E> entry) {
        return access.get(entry.getType()).map(e -> entry.getValueGetter().applyAsDouble(e));
    }

    private static <E extends EntityComponent> boolean runSetters(ComponentAccess access, ChangeableEntry<E> entry, int value) {
        return access.get(entry.getType()).map(e -> {
            entry.getValueSetter().accept(e, value);
            e.syncToClient();
            return true;
        }).orElse(false);
    }

    @Value
    private static class ChangeableEntry<E extends EntityComponent> {
        String name;
        RegistryObject<? extends EntityComponentType<E, ?>> type;
        ToDoubleFunction<E> valueGetter;
        BiConsumer<E, Integer> valueSetter;
    }
}
