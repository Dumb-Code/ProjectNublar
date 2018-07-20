package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ItemDinosaurMeat extends ItemFood implements DinosaurStack {

    public ItemDinosaurMeat() {
        super(1, 1, true); // amount and saturation are overridden with methods to depend on the dinosaur
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(getValue(stack).getRegName().toString());
        tooltip.add(getVarient(stack).toString());

        int[] ids = OreDictionary.getOreIDs(stack);
        for(int i=0;i<ids.length;i++) {
            tooltip.add(TextFormatting.BOLD.toString()+OreDictionary.getOreName(ids[i]));
        }

    }

    @Override
    public int getHealAmount(ItemStack stack) {
        Dinosaur dino = getValue(stack);
        ItemProperties itemProperties = dino.getItemProperties();
        if(getVarient(stack) == CookState.COOKED) {
            return itemProperties.getCookedMeatHealAmount();
        }
        return itemProperties.getRawMeatHealAmount();
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        Dinosaur dino = getValue(stack);
        ItemProperties itemProperties = dino.getItemProperties();
        if(getVarient(stack) == CookState.COOKED) {
            return itemProperties.getCookedMeatSaturation();
        }
        return itemProperties.getRawMeatSaturation();
    }

    @Nonnull
    @Override
    public CookState getVarient(ItemStack stack) {
        boolean isCooked = stack.getOrCreateSubCompound(ProjectNublar.MODID).getBoolean("cooked");
        if(isCooked)
            return CookState.COOKED;
        return CookState.RAW;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subtypes) {
        if(this.isInCreativeTab(tab)) {
            subtypes.addAll(getAllStacksWithVarients());
        }
    }

    public List<ItemStack> getAllStacksWithVarients() {
        List<ItemStack> result = new LinkedList<>();
        List<ItemStack> stacksWithDinos = this.getAllStacksOrdered();
        for(ItemStack stack : stacksWithDinos) {
            ItemStack raw = stack.copy();
            ItemStack cooked = stack.copy();
            raw.getOrCreateSubCompound(ProjectNublar.MODID).setBoolean("cooked", false);
            cooked.getOrCreateSubCompound(ProjectNublar.MODID).setBoolean("cooked", true);
            result.add(raw);
            result.add(cooked);
        }
        return result;
    }

    public static ItemStack createMeat(Dinosaur dinosaur, CookState cookState) {
        ItemDinosaurMeat meatItem = (ItemDinosaurMeat)NublarItems.DINOSAUR_MEAT;
        ItemStack result = new ItemStack(meatItem);
        meatItem.putValue(result, dinosaur);
        result.getOrCreateSubCompound(ProjectNublar.MODID).setBoolean("cooked", cookState == CookState.COOKED);
        return result;
    }

    public void registerOreNames() {
        for(Dinosaur dino : ProjectNublar.DINOSAUR_REGISTRY) {

            ItemStack rawStack = dino.getCachedItems().getRawMeat().copy();
            ItemStack cookedStack = dino.getCachedItems().getCookedMeat().copy();

            OreDictionary.registerOre("meatDinosaur", rawStack);
            OreDictionary.registerOre("meatDinosaur"+dino.getOreSuffix(), rawStack);
            OreDictionary.registerOre("meatDinosaurRaw"+dino.getOreSuffix(), rawStack);

            OreDictionary.registerOre("meatDinosaur", cookedStack);
            OreDictionary.registerOre("meatDinosaur"+dino.getOreSuffix(), cookedStack);
            OreDictionary.registerOre("meatDinosaurCooked"+dino.getOreSuffix(), cookedStack);
        }
    }

    public enum CookState {
        RAW, COOKED
    }
}
