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

public class MachineModuleParts {
    public static final MachineModulePart[] TEST_MACHINES = {

        simplePartItem(MachineModuleType.TEST, null, () -> Items.APPLE, () -> Items.STICK)
    };

    public static final MachineModulePart[] SEQUENCING_SYNTHESIZER = {
        simplePartItem(MachineModuleType.COMPUTER_CHIP, null, () -> ItemHandler.COMPUTER_CHIP_PART),
        simplePartItem(MachineModuleType.TANKS, null, () -> ItemHandler.TANKS_PART),
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
            .tiers(Arrays.stream(tiers).map(MachineModuleParts::convert).collect(Collectors.toList()))
            .build();
    }

    private static Predicate<ItemStack> convert(Supplier<Item> itemSupplier) {
        return stack -> stack.getItem() == itemSupplier.get();
    }
}
