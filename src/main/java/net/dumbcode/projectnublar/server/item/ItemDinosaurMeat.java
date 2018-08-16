package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class ItemDinosaurMeat extends ItemFood implements DinosaurProvider, ItemWithOreName {

    @Getter
    private final Dinosaur dinosaur;
    private final CookState cookState;

    public ItemDinosaurMeat(Dinosaur dinosaur, CookState cookState) {
        super(1, 1, true); // amount and saturation are overridden with methods to depend on the dinosaur
        this.dinosaur = dinosaur;
        this.cookState = cookState;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getHealAmount(ItemStack stack) {
        ItemProperties itemProperties = dinosaur.getItemProperties();
        if(cookState == CookState.COOKED) {
            return itemProperties.getCookedMeatHealAmount();
        }
        return itemProperties.getRawMeatHealAmount();
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        ItemProperties itemProperties = dinosaur.getItemProperties();
        if(cookState == CookState.COOKED) {
            return itemProperties.getCookedMeatSaturation();
        }
        return itemProperties.getRawMeatSaturation();
    }

    public void registerOreNames() {
        OreDictionary.registerOre("meatDinosaur", this);
        if(cookState == CookState.RAW) {
            OreDictionary.registerOre("meatDinosaurRaw", this);
            OreDictionary.registerOre("meatDinosaurRaw"+dinosaur.getOreSuffix(), this);
        }
        else {
            OreDictionary.registerOre("meatDinosaurCooked", this);
            OreDictionary.registerOre("meatDinosaurCooked"+dinosaur.getOreSuffix(), this);
        }
        OreDictionary.registerOre("meatDinosaur"+dinosaur.getOreSuffix(), this);
    }

    @Override
    public String getMostSpecificOreName() {
        return "meatDinosaur"+cookState.getOrePart()+dinosaur.getOreSuffix();
    }

    public enum CookState {
        RAW("Raw"), COOKED("Cooked");

        @Getter
        private final String orePart;

        CookState(String orePart) {
            this.orePart = orePart;
        }
    }
}
