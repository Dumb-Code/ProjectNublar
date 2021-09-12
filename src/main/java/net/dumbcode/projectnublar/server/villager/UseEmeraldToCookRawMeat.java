package net.dumbcode.projectnublar.server.villager;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;

import javax.annotation.Nullable;
import java.util.Random;

@RequiredArgsConstructor
public class UseEmeraldToCookRawMeat implements VillagerTrades.ITrade {
    private final int conversionAmount;
    private final int emeraldAmount;

    private final int maxTrades;
    private final int xp;
    private final float multiplier;

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity entity, Random random) {
        Dinosaur dinosaur = Dinosaur.getRandom();
        Item buyItem = ItemHandler.RAW_MEAT_ITEMS.get(dinosaur);
        Item sellItem = ItemHandler.COOKED_MEAT_ITEMS.get(dinosaur);
        return new MerchantOffer(
            new ItemStack(Items.EMERALD, this.emeraldAmount),
            new ItemStack(buyItem, this.conversionAmount),
            new ItemStack(sellItem, this.conversionAmount),
            this.maxTrades, this.xp, this.multiplier
        );
    }
}
