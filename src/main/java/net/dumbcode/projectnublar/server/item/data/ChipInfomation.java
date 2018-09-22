package net.dumbcode.projectnublar.server.item.data;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

public class ChipInfomation {

    public static ItemStack combine(ItemStack syringe, ItemStack chip) {
        NBTTagCompound nbt = chip.getOrCreateSubCompound(ProjectNublar.MODID);
        nbt.setInteger("percent", nbt.getInteger("percent") + new Random().nextInt(20));
        return chip;
    }
}
