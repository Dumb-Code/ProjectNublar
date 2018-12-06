package net.dumbcode.projectnublar.server.item;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineModule implements Predicate<ItemStack>, IStringSerializable {

    public static final MachineModule[] TEST_MACHINES = {
            new MachineModule("test1",() -> Items.APPLE)
    };

    private final Predicate<ItemStack> itemPredicate;
    private final String name;

    private MachineModule(String name, Supplier<Item> itemSupplier) {
        this(name, stack -> stack.getItem() == itemSupplier.get());
    }

    private MachineModule(String name, Predicate<ItemStack> itemPredicate) {
        this.name = name;
        this.itemPredicate = itemPredicate;
    }

    @Override
    public boolean test(ItemStack stack) {
        return itemPredicate.test(stack);
    }

    @Override
    public String getName() {
        return this.name.toLowerCase();
    }
}
