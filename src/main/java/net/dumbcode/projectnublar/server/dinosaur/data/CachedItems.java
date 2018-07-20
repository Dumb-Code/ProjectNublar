package net.dumbcode.projectnublar.server.dinosaur.data;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.minecraft.item.ItemStack;

@Getter
public class CachedItems {

    private ItemStack rawMeat;
    private ItemStack cookedMeat;

    public CachedItems(Dinosaur dinosaur) {
        this.rawMeat = ItemDinosaurMeat.createMeat(dinosaur, ItemDinosaurMeat.CookState.RAW);
        this.cookedMeat = ItemDinosaurMeat.createMeat(dinosaur, ItemDinosaurMeat.CookState.COOKED);
    }

}
