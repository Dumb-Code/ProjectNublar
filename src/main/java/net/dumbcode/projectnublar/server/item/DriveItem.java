package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DriveItem extends Item {

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound nbt = stack.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");
        for (String key : nbt.getKeySet()) {
            tooltip.add(key + ": " + nbt.getCompoundTag(key).getInteger("amount"));
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
