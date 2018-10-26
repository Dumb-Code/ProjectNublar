package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants;

public class ItemSyringe extends Item implements DriveUtils.DriveInformation {

    private final boolean filled;

    public ItemSyringe(boolean filled) {
        this.filled = filled;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        ItemStack out = new ItemStack(ItemHandler.FILLED_SYRINGE);
        NBTTagCompound nbt = out.getOrCreateSubCompound(ProjectNublar.MODID);
        String s = EntityList.getEntityString(entity);
        if(s == null) {
            s = "generic";//Shouldn't happen, but its how vanilla handles it
        }
        nbt.setString("ContainedType", s);
        nbt.setInteger("ContainedSize", MathUtils.getWeightedResult(50));// TODO: 16/10/2018 make a better number generator
        player.setHeldItem(EnumHand.MAIN_HAND, out);
        return false;
    }

    @Override
    public int getSize(ItemStack stack) {
        return stack.getOrCreateSubCompound(ProjectNublar.MODID).getInteger("ContainedSize");
    }

    @Override
    public String getKey(ItemStack stack) {
        return stack.getOrCreateSubCompound(ProjectNublar.MODID).getString("ContainedType");
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "entity." + this.getKey(stack) + ".name";
    }

    @Override
    public boolean hasInformation(ItemStack stack) {
        return this.filled && stack.getOrCreateSubCompound(ProjectNublar.MODID).hasKey("ContainedType", Constants.NBT.TAG_STRING);
    }

    @Override
    public ItemStack getOutItem(ItemStack stack) {
        return new ItemStack(ItemHandler.EMPTY_SYRINGE);
    }
}
