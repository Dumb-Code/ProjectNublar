package net.dumbcode.projectnublar.server.recipes.crafting;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ArtificialEggAddDnaCraftingRecipe extends SpecialRecipe {

    public ArtificialEggAddDnaCraftingRecipe(ResourceLocation location) {
        super(location);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean egg = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() == ItemHandler.ARTIFICIAL_EGG.get()) {
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
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack egg = ItemStack.EMPTY;
        ItemStack dna = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() == ItemHandler.ARTIFICIAL_EGG.get()) {
                    egg = stack;
                } else if(stack.getItem() instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_DNA.containsValue(stack.getItem())) {
                    dna = stack;
                }
            }
        }
        if(egg.isEmpty() || dna.isEmpty()) {
            return ItemStack.EMPTY;
        }

        System.out.println(dna.getTag());

        ItemStack out = ItemStack.EMPTY;
        if(dna.getItem() instanceof DinosaurProvider) {
            out = new ItemStack(ItemHandler.DINOSAUR_UNINCUBATED_EGG.get(((DinosaurProvider) dna.getItem()).getDinosaur()));
            out.getOrCreateTagElement(ProjectNublar.MODID).put("Genetics", dna.getOrCreateTagElement(ProjectNublar.MODID).getList("Genetics", Constants.NBT.TAG_COMPOUND).copy());
        }
        return out;

    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return null;
    }

}
