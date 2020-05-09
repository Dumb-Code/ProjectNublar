package net.dumbcode.projectnublar.server.item;

import lombok.RequiredArgsConstructor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
public class FilterItem extends Item {

    private final float efficiency;

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.MENDING && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public float getEfficiency(ItemStack stack) {
        return this.efficiency * (1F - (0.75F * (float)stack.getItemDamage()/stack.getMaxDamage()));
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if(entityItem.isInWater()) {
            entityItem.getItem().setItemDamage(0);
        }
        return super.onEntityItemUpdate(entityItem);
    }
}
