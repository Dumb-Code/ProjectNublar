package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MachineModuleParts {
    public static final MachineModulePart[] TEST_MACHINES = {
        simplePartItem(MachineModuleType.TEST, null, () -> Items.APPLE, () -> Items.STICK)
    };

    public static final MachineModulePart[] DRILL_EXTRACTOR = {
        simplePartItemMeta(MachineModuleType.DRILL_BIT, null, 5, () -> ItemHandler.DRILL_BIT_PART)
    };

    public static final MachineModulePart[] FOSSIL_PROCESSOR = {
        simplePartItemMeta(MachineModuleType.COMPUTER_CHIP, null, 3, () -> ItemHandler.COMPUTER_CHIP_PART),
        simplePartItemMeta(MachineModuleType.TANKS, null, 2, () -> ItemHandler.TANKS_PART),
    };

    public static final MachineModulePart[] SEQUENCING_SYNTHESIZER = {
        simplePartItemMeta(MachineModuleType.COMPUTER_CHIP, null, 2, () -> ItemHandler.COMPUTER_CHIP_PART),
        simplePartItemMeta(MachineModuleType.TANKS, null, 4, () -> ItemHandler.TANKS_PART),
    };

    public static final MachineModulePart[] EGG_PRINTER = {
        simplePartItemMeta(MachineModuleType.COMPUTER_CHIP, null, 3, () -> ItemHandler.COMPUTER_CHIP_PART),
        simplePartItem(MachineModuleType.LEVELING_SENSORS, null, () -> ItemHandler.LEVELLING_SENSOR_PART),
    };

    public static final MachineModulePart[] INCUBATOR = {
        simplePartItemMeta(MachineModuleType.BULB, null, 3, () -> ItemHandler.BULB_PART),
        simplePartItemMeta(MachineModuleType.CONTAINER, null, 2, () -> ItemHandler.CONTAINER_PART), //todo: implement this
        simplePartItemMeta(MachineModuleType.TANKS, null, 2, () -> ItemHandler.TANKS_PART),
    };

    public static final MachineModulePart[] COAL_GENERATOR = {
        simplePartItemMeta(MachineModuleType.TURBINES, null, 2, () -> ItemHandler.TURBINES_PART)
    };

    @SafeVarargs
    public static MachineModulePart simplePart(MachineModuleType type, @Nullable MachineModuleType dependency, Predicate<ItemStack>... tiers) {
        return MachineModulePart.builder()
            .type(type)
            .dependency(dependency)
            .tiers(Lists.newArrayList(tiers))
            .build();
    }

    public static MachineModulePart simplePartItemMeta(MachineModuleType type, @Nullable MachineModuleType dependency, int meta, Supplier<Item> item) {
        return MachineModulePart.builder()
            .type(type)
            .dependency(dependency)
            .tiers(IntStream.range(0, meta).<Predicate<ItemStack>>mapToObj(i -> stack -> stack.getItem() == item.get() && stack.getMetadata() == i).collect(Collectors.toList()))
            .build();
    }

    @SafeVarargs
    public static MachineModulePart simplePartItem(MachineModuleType type, @Nullable MachineModuleType dependency, Supplier<Item>... tiers) {
        return MachineModulePart.builder()
            .type(type)
            .dependency(dependency)
            .tiers(Arrays.stream(tiers).<Predicate<ItemStack>>map(itemSupplier -> stack -> stack.getItem() == itemSupplier.get()).collect(Collectors.toList()))
            .build();
    }
}
