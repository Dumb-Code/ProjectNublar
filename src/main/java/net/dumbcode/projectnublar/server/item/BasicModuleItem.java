package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public class BasicModuleItem extends Item implements ModuleItem {

    private final Supplier<TabletModuleType<?>> moduleType;

    public BasicModuleItem(Supplier<TabletModuleType<?>> moduleType, Properties properties) {
        super(properties);
        this.moduleType = moduleType;
    }

    @Override
    public TabletModuleType<?> getType() {
        return this.moduleType.get();
    }
}
