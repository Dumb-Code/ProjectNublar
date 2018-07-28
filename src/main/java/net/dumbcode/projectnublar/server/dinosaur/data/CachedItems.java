package net.dumbcode.projectnublar.server.dinosaur.data;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemDinosaurMeat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Getter
public class CachedItems {

    private ItemDinosaurMeat rawMeat;
    private ItemDinosaurMeat cookedMeat;

    public CachedItems(Dinosaur dinosaur) {
        this.rawMeat = new ItemDinosaurMeat(dinosaur, ItemDinosaurMeat.CookState.RAW);
        this.cookedMeat = new ItemDinosaurMeat(dinosaur, ItemDinosaurMeat.CookState.COOKED);
    }

}
