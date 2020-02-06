package net.dumbcode.projectnublar.server.item;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.item.Item;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class BasicModuleItem extends Item implements ModuleItem {

    private final Supplier<TabletModuleType<?>> typeGetter;

    @Override
    public TabletModuleType<?> getType() {
        return this.typeGetter.get();
    }
}
