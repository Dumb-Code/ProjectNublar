package net.dumbcode.projectnublar.server.recipes.crafting;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.DnaHoverDinosaurItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

        ItemStack out = ItemStack.EMPTY;
        if(dna.getItem() instanceof DinosaurProvider) {
            out = new ItemStack(ItemHandler.DINOSAUR_UNINCUBATED_EGG.get(((DinosaurProvider) dna.getItem()).getDinosaur()));
            DnaHoverDinosaurItem.copyDataNBT(dna, out);
        }
        return out;

    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ProjectNublarRecipesSerializers.ARTIFICIAL_EGG_ADD_DNA.get();
    }

}
