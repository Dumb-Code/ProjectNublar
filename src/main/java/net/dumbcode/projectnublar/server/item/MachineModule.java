package net.dumbcode.projectnublar.server.item;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineModule {

    public static final MachineModule[] TEST_MACHINES = {
            new MachineModule("test1",() -> Items.APPLE)
    };

    public static final MachineModule MACHINE_BASE = new MachineModule("machine_base", () -> ItemHandler.MACHINE_BASE_PART);
    public static final MachineModule COMPUTER = new MachineModule("computer", () -> ItemHandler.COMPUTER_PART);
    public static final MachineModule MONITOR = new MachineModule("monitor", () -> ItemHandler.MONITOR_PART);
    public static final MachineModule DOOR = new MachineModule("door", () -> ItemHandler.DOOR_PART);
    public static final MachineModule MOTOR = new MachineModule("arm", () -> ItemHandler.DOOR_PART);
    public static final MachineModule ARM = new MachineModule("arm", () -> ItemHandler.DOOR_PART);

    public static final MachineModule[] SEQUENCING_SYNTHESIZER = {
        MACHINE_BASE,
        COMPUTER,
        MONITOR.withDependsOn(COMPUTER),
        DOOR.withDependsOn(MACHINE_BASE)
    };



    private final Predicate<ItemStack> itemPredicate;
    private final String name;
    private final List<MachineModule> dependents = new ArrayList<>();

    private MachineModule(String name, Supplier<Item> itemSupplier) {
        this(name, stack -> stack.getItem() == itemSupplier.get());
    }

    private MachineModule(String name, Predicate<ItemStack> itemPredicate) {
        this.name = name;
        this.itemPredicate = itemPredicate;
    }

    public MachineModule withDependsOn(MachineModule... modules) {
        MachineModule module = new MachineModule(this.name, this.itemPredicate);
        Collections.addAll(this.dependents, modules);
        return module;
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
