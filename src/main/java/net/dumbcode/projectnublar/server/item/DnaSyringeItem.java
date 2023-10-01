package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class DnaSyringeItem extends Item implements DriveUtils.DriveInformation {

    public DnaSyringeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getSize(ItemStack stack) {
        return stack.getOrCreateTagElement(ProjectNublar.MODID).getInt("ContainedSize");
    }

    @Override
    public String getKey(ItemStack stack) {
        return stack.getOrCreateTagElement(ProjectNublar.MODID).getString("ContainedType");
    }

    @Override
    public String getAnimalVariant(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
        return nbt.contains("ContainedVariant") ? nbt.getString("ContainedVariant") : null;
    }

    @Override
    public String getDriveTranslationKey(ItemStack stack) {
        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(this.getKey(stack)));
        if(value == null) {
            return "missing";
        }
        return value.getDescriptionId();
    }

    @Override
    public boolean hasInformation(ItemStack stack) {
        return stack.getOrCreateTagElement(ProjectNublar.MODID).contains("ContainedType", Constants.NBT.TAG_STRING);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(
            new TranslationTextComponent("item.projectnublar.dna_filled_syringe.contains",
                this.getSize(stack),
                DriveUtils.getTranslation(this.getDriveTranslationKey(stack), this.getAnimalVariant(stack))
            ).withStyle(TextFormatting.GRAY)
        );
        super.appendHoverText(stack, world, tooltip, flagIn);
    }


    @Override
    public ItemStack getOutItem(ItemStack stack) {
        return new ItemStack(ItemHandler.EMPTY_SYRINGE.get());
    }
}
