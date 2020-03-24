package net.dumbcode.projectnublar.server.item;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineModule {

    public static final MachineModule[] TEST_MACHINES = {
            new MachineModule("test1",() -> Items.APPLE)
    };

    public static final MachineModule MACHINE_BASE = new MachineModule("machine_base", () -> ItemHandler.MACHINE_BASE_PART);
    public static final MachineModule COMPUTER = new MachineModule("computer", () -> ItemHandler.COMPUTER_PART);
    public static final MachineModule MONITOR = new MachineModule("monitor", () -> ItemHandler.MONITOR_PART, COMPUTER);
    public static final MachineModule DOOR = new MachineModule("door", () -> ItemHandler.DOOR_PART, MACHINE_BASE);

    private final Predicate<ItemStack> itemPredicate;
    private final String name;
    private final MachineModule[] dependents;

    private MachineModule(String name, Supplier<Item> itemSupplier, MachineModule... dependents) {
        this(name, stack -> stack.getItem() == itemSupplier.get(), dependents);
    }

    private MachineModule(String name, Predicate<ItemStack> itemPredicate, MachineModule... dependents) {
        this.name = name;
        this.itemPredicate = itemPredicate;
        this.dependents = dependents;
    }

    public boolean testDependents(List<MachineModule> orderList, int state) {
        boolean result = true;
        for (MachineModule dependent : this.dependents) {
            if(!orderList.contains(dependent)) {
                throw new IllegalArgumentException("Dependent '" + dependent.getName() + "' from '" + this.getName() + "' doesnt exist in block");
            }
            result &= (state & (int) Math.pow(2, orderList.indexOf(dependent))) != 0;
        }
        return result;
    }

    public boolean testStack(ItemStack stack) {
        return itemPredicate.test(stack);
    }

    public String getName() {
        return this.name.toLowerCase();
    }
}
