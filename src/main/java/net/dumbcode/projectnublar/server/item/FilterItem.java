package net.dumbcode.projectnublar.server.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FilterItem extends Item {

    private final float efficiency;

    public FilterItem(float efficiency, Properties properties) {
        super(properties);
        this.efficiency = efficiency;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.MENDING && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public float getEfficiency(ItemStack stack) {
        return this.efficiency * (1F - (0.75F * (float)stack.getDamageValue()/stack.getMaxDamage()));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isInWater()) {
            stack.setDamageValue(0);
        }
        return false;
    }
}
