package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import java.util.List;

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

    public static List<DriveEntry> getAll(ItemStack drive) {
        List<DriveEntry> out = Lists.newArrayList();
        NBTTagCompound nbt = drive.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");

        for (String s : nbt.getKeySet()) {
            NBTTagCompound tag = nbt.getCompoundTag(s);
            out.add(new DriveEntry(s, tag.getString("translation_key"), tag.getInteger("amount"), DriveType.values()[tag.getInteger("drive_type") % DriveType.values().length]));
        }

        return out;
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
        inner.setString("translation_key", info.getTranslationKey(inItem));
        inner.setInteger("drive_type", info.getDriveType(inItem).ordinal());
        nbt.setTag(key, inner);

        drive.getOrCreateSubCompound(ProjectNublar.MODID).setTag("drive_information", nbt);
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
        String getTranslationKey(ItemStack stack);
        default DriveType getDriveType(ItemStack stack) {
            return DriveType.OTHER;
        }
        default boolean hasInformation(ItemStack stack) {
            return true;
        }
        default ItemStack getOutItem(ItemStack stack) {
            return ItemStack.EMPTY;
        }
    }

    public static class DriveEntry {
        private final String key;
        private final String name;
        private final int amount;
        private final DriveType driveType;

        public DriveEntry(String key, String name, int amount, DriveType driveType) {
            this.key = key;
            this.name = name;
            this.amount = amount;
            this.driveType = driveType;
        }

        public String getKey() {
            return key;
        }

        public DriveType getDriveType() {
            return driveType;
        }

        public String getName() {
            return this.name;
        }

        public int getAmount() {
            return this.amount;
        }
    }

    public enum DriveType {
        DINOSAUR,
        OTHER
    }

}
