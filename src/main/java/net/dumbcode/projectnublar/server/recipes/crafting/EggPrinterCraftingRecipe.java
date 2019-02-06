package net.dumbcode.projectnublar.server.recipes.crafting;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class EggPrinterCraftingRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean egg = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() == ItemHandler.ARTIFICIAL_EGG) {
                    if(egg) {
                        return false;
                    } else {
                        egg = true;
                    }
                } else if(!(stack.getItem() instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_DNA.containsValue(stack.getItem()))) {
                    return false;
                }
            }
        }
        return egg;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack egg = ItemStack.EMPTY;
        ItemStack dna = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() == ItemHandler.ARTIFICIAL_EGG) {
                    egg = stack;
                } else if(stack.getItem() instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_DNA.containsValue(stack.getItem())) {
                    dna = stack;
                }
            }
        }
        if(egg.isEmpty() || dna.isEmpty()) {
            return ItemStack.EMPTY;
        }

        System.out.println(dna.getTagCompound());

        ItemStack out = ItemStack.EMPTY;
        if(dna.getItem() instanceof DinosaurProvider) {
            out = new ItemStack(ItemHandler.DINOSAUR_UNINCUBATED_EGG.get(((DinosaurProvider) dna.getItem()).getDinosaur()));
            out.getOrCreateSubCompound(ProjectNublar.MODID).setTag("dna_info", dna.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("dna_info").copy());
        }
        return out;

    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}
