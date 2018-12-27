package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemSyringe extends Item implements DriveUtils.DriveInformation {

    private final Type type;

    public ItemSyringe(Type type) {
        this.type = type;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        ItemStack out;
        if((entity instanceof EntityChicken || entity instanceof EntityParrot) && ((EntityAnimal) entity).isInLove()) {
            out = new ItemStack(ItemHandler.EMBRYO_FILLED_SYRINGE);
            out.getOrCreateSubCompound(ProjectNublar.MODID).setString("ContainedType", Objects.requireNonNull(EntityList.getEntityString(entity)));
        } else {
            out = new ItemStack(ItemHandler.DNA_FILLED_SYRINGE);
            NBTTagCompound nbt = out.getOrCreateSubCompound(ProjectNublar.MODID);
            String s = EntityList.getEntityString(entity);
            if(s == null) {
                s = "generic";//Shouldn't happen, but its how vanilla handles it
            }
            nbt.setString("ContainedType", s);
            nbt.setInteger("ContainedSize", MathUtils.getWeightedResult(50));// TODO: 16/10/2018 make a better number generator
        }
        stack.shrink(1);
        if(stack.isEmpty()) {
            player.setHeldItem(EnumHand.MAIN_HAND, out);
        } else {
            MachineUtils.giveToInventory(player, out);
        }
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
        return this.type == Type.FILLED_DNA && stack.getOrCreateSubCompound(ProjectNublar.MODID).hasKey("ContainedType", Constants.NBT.TAG_STRING);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(this.type.filled) {
            tooltip.add(I18n.format(this.getTranslationKey(stack)));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ItemStack getOutItem(ItemStack stack) {
        return new ItemStack(ItemHandler.EMPTY_SYRINGE);
    }

    public enum Type {
        EMPTY(false), FILLED_DNA(true), FILLED_EMBRYO(true);

        private final boolean filled;

        Type(boolean filled) {
            this.filled = filled;
        }
    }
}
