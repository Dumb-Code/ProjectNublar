package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class DinosaurGeneticMaterialItem extends BasicDinosaurItem implements DriveUtils.DriveInformation {

    private final String key;
    private final int size;

    public DinosaurGeneticMaterialItem(Dinosaur dinosaur, String key, int size) {
        super(dinosaur);
        this.key = key;
        this.size = size;
    }

    @Override
    public int getSize(ItemStack stack) {
        return MathUtils.getWeightedResult(this.size, this.size / 2D);
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
    public String getTranslationKey(ItemStack stack) {
        ResourceLocation regName = this.getDinosaur().getRegName();
        return regName.getNamespace()+".dino."+regName.getPath();
    }

    @Override
    public String getDriveTranslationKey(ItemStack stack) {
        return this.getTranslationKey(stack) + ".name";
    }
}
