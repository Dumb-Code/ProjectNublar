package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer;

import java.util.List;
import java.util.Random;

public class DriveUtils {

    public static boolean canAdd(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return false;
        }
        DriveInformation info = (DriveInformation) inItem.getItem();
        String key = info.getKey(inItem);
        if(key.isEmpty()) {
            return false;
        }
        return drive.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information").getCompoundTag(key).getInteger("amount") < 100;
    }

    public static void addItemToDrive(ItemStack drive, ItemStack inItem) {

        if (!(inItem.getItem() instanceof DriveInformation)) {
            return;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();
        NBTTagCompound nbt = drive.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");
        String key = info.getKey(inItem);
        if(key.isEmpty()) {
            return;
        }
        NBTTagCompound inner = nbt.getCompoundTag(key);
        int current = inner.getInteger("amount");
        if(current >= 100) {
            return;
        }
        int result = MathUtils.getWeightedResult(info.getSize(inItem));

        inner.setInteger("amount", MathHelper.clamp(current + result, 0, 100));
        nbt.setTag(key, inner);

        drive.getOrCreateSubCompound(ProjectNublar.MODID).setTag("drive_information", nbt);
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
        default boolean hasInformation(ItemStack stack) {
            return true;
        }
        default ItemStack getOutItem(ItemStack stack) {
            return ItemStack.EMPTY;
        }
    }

}
