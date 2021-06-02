package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class DinosaurGeneticMaterialItem extends BasicDinosaurItem implements DriveUtils.DriveInformation {

    private final String key;

    public DinosaurGeneticMaterialItem(Dinosaur dinosaur, Properties properties) {
        super(dinosaur, properties);
        this.key = dinosaur.getRegName().toString();
    }


    @Override
    public int getSize(ItemStack stack) {
        NBTTagCompound compound = stack.getSubCompound(ProjectNublar.MODID);
        return compound != null && compound.hasKey("GeneticMaterialSize", 99)? compound.getInteger("GeneticMaterialSize") : 2;
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

    public static ItemStack setSize(ItemStack stack, int size) {
        if(stack.getItem() instanceof DinosaurGeneticMaterialItem) {
            stack.getOrCreateSubCompound(ProjectNublar.MODID).setInteger("GeneticMaterialSize", size);
        }
        return stack;
    }
}
