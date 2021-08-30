package net.dumbcode.projectnublar.server.item;

import net.minecraft.item.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MachineModuleBuildPart {
    private final String name;
    private final Supplier<Item> item;
    private final String[] dependencies;

    @SafeVarargs
    public MachineModuleBuildPart(String name, Supplier<Item> item, String... dependencies) {
        this.name = name;
        this.item = item;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item.get();
    }

    public String[] getDependencies() {
        return dependencies;
    }
}
