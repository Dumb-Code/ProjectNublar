package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DinosaurGeneticMaterialItem extends BasicDinosaurItem implements DriveUtils.DriveInformation {

    private final String key;

    public DinosaurGeneticMaterialItem(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(dinosaur, translationKey, properties);
        this.key = dinosaur.getRegName().toString();
    }


    @Override
    public int getSize(ItemStack stack) {
        CompoundNBT compound = stack.getTagElement(ProjectNublar.MODID);
        return compound != null && compound.contains("GeneticMaterialSize", 99)? compound.getInt("GeneticMaterialSize") : 2;
    }

    @Override
    public String getKey(ItemStack stack) {
        return this.key;
    }

    @Override
    public DriveUtils.DriveType getDriveType(ItemStack stack) {
        return DriveUtils.DriveType.DINOSAUR;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> components, ITooltipFlag flag) {
        components.add(ProjectNublar.translate("item.genetic_material_test_tube.size", this.getSize(stack)).withStyle(TextFormatting.GRAY));
        super.appendHoverText(stack, world, components, flag);
    }

    public String getTranslationKey(ItemStack stack) {
        ResourceLocation regName = this.getDinosaur().getRegName();
        return regName.getNamespace()+".dino."+regName.getPath();
    }

    @Override
    public String getDriveTranslationKey(ItemStack stack) {
        return this.getTranslationKey(stack) + ".name";
    }

    public static ItemStack setSize(ItemStack stack, int size) {
        if(stack.getItem() instanceof DinosaurGeneticMaterialItem) {
            stack.getOrCreateTagElement(ProjectNublar.MODID).putInt("GeneticMaterialSize", size);
        }
        return stack;
    }

    @Override
    public ItemStack getOutItem(ItemStack stack) {
        return new ItemStack(ItemHandler.EMPTY_TEST_TUBE.get());
    }
}
