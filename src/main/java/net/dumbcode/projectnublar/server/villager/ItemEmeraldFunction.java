package net.dumbcode.projectnublar.server.villager;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.Random;

@RequiredArgsConstructor
public class ItemEmeraldFunction implements EntityVillager.ITradeList {

    private final Item buyItem;
    private final Item sellItem;

    private final EntityVillager.PriceInfo conversionAmount;
    private final EntityVillager.PriceInfo emeraldAmount;

    @Override
    public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
        int amount = this.conversionAmount.getPrice(random);
        recipeList.add(new MerchantRecipe(new ItemStack(this.buyItem, amount), new ItemStack(Items.EMERALD, this.emeraldAmount.getPrice(random)), new ItemStack(this.sellItem, amount)));
    }
}
