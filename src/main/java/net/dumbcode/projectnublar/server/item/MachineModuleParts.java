package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MachineModuleParts {
    public static final MachineModulePart[] DRILL_EXTRACTOR = {
        simplePartItem(MachineModuleType.DRILL_BIT, null, ItemHandler.DRILL_BIT_PART_1,
            ItemHandler.DRILL_BIT_PART_2, ItemHandler.DRILL_BIT_PART_3,
            ItemHandler.DRILL_BIT_PART_4, ItemHandler.DRILL_BIT_PART_5
        )
    };

    public static final MachineModulePart[] FOSSIL_PROCESSOR = {
        simplePartItem(MachineModuleType.COMPUTER_CHIP, null, ItemHandler.COMPUTER_CHIP_PART_1, ItemHandler.COMPUTER_CHIP_PART_2, ItemHandler.COMPUTER_CHIP_PART_3),
        simplePartItem(MachineModuleType.TANKS, null, ItemHandler.TANKS_PART_1, ItemHandler.TANKS_PART_2),
    };

    public static final MachineModulePart[] SEQUENCING_SYNTHESIZER = {
        simplePartItem(MachineModuleType.COMPUTER_CHIP, null, ItemHandler.COMPUTER_CHIP_PART_1, ItemHandler.COMPUTER_CHIP_PART_2),
        simplePartItem(MachineModuleType.TANKS, null, ItemHandler.TANKS_PART_1, ItemHandler.TANKS_PART_2, ItemHandler.TANKS_PART_3, ItemHandler.TANKS_PART_4),
    };

    public static final MachineModulePart[] EGG_PRINTER = {
        simplePartItem(MachineModuleType.COMPUTER_CHIP, null, ItemHandler.COMPUTER_CHIP_PART_1, ItemHandler.COMPUTER_CHIP_PART_2, ItemHandler.COMPUTER_CHIP_PART_3),
        simplePartItem(MachineModuleType.LEVELING_SENSORS, null, ItemHandler.LEVELLING_SENSOR_PART),
    };

    public static final MachineModulePart[] INCUBATOR = {
        simplePartItem(MachineModuleType.BULB, null, ItemHandler.BULB_PART_1, ItemHandler.BULB_PART_2, ItemHandler.BULB_PART_3),
        simplePartItem(MachineModuleType.CONTAINER, null, ItemHandler.CONTAINER_PART_1, ItemHandler.CONTAINER_PART_2, ItemHandler.CONTAINER_PART_3), //todo: implement this
        simplePartItem(MachineModuleType.TANKS, null, ItemHandler.TANKS_PART_1, ItemHandler.TANKS_PART_2),
    };

    public static final MachineModulePart[] COAL_GENERATOR = {
        simplePartItem(MachineModuleType.TURBINES, null, ItemHandler.TURBINES_PART_1, ItemHandler.TURBINES_PART_2)
    };

    @SafeVarargs
    public static MachineModulePart simplePart(MachineModuleType type, @Nullable MachineModuleType dependency, Predicate<ItemStack>... tiers) {
        return MachineModulePart.builder()
            .type(type)
            .dependency(dependency)
            .tiers(Lists.newArrayList(tiers))
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
