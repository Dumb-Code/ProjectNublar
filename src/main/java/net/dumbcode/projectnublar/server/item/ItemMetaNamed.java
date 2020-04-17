package net.dumbcode.projectnublar.server.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class ItemMetaNamed extends Item {

    private final int subTypes;

    public ItemMetaNamed(int subTypes) {
        this.subTypes = subTypes;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if(this.isInCreativeTab(tab)) {
            for (int i = 0; i < this.subTypes; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public int getMetadata(int damage) {
        return MathHelper.clamp(damage, 0, this.subTypes);
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return this.getMetadata(super.getMetadata(stack));
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + stack.getMetadata();
    }

    public int getSubTypes() {
        return subTypes;
    }
}
