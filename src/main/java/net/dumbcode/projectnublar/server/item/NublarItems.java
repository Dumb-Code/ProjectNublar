package net.dumbcode.projectnublar.server.item;

import net.minecraft.item.Item;

public final class NublarItems {
    public static ItemDinosaurMeat DINOSAUR_MEAT = null;

    public static Item[] getAllItems() {
        return new Item[] {
                DINOSAUR_MEAT,
        };
    }
}
