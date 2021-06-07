package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
        return drive.getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information").getCompound(key).getInt("amount") < 100;
    }

    public static List<DriveEntry> getAll(ItemStack drive) {
        List<DriveEntry> out = Lists.newArrayList();
        CompoundNBT nbt = drive.getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information");

        for (String s : nbt.getAllKeys()) {
            CompoundNBT tag = nbt.getCompound(s);
            out.add(new DriveEntry(s, tag.getString("translation_key"), tag.getInt("amount"), DriveType.values()[tag.getInt("drive_type") % DriveType.values().length]));
        }

        return out;
    }

    public static int getAmount(ItemStack drive, String key) {
        return drive.getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information").getCompound(key).getInt("amount");
    }

    public static void addItemToDrive(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();
        CompoundNBT nbt = drive.getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information");
        String key = info.getKey(inItem);
        if(key.isEmpty()) {
            return;
        }
        CompoundNBT inner = nbt.getCompound(key);
        int current = inner.getInt("amount");
        if(current >= 100) {
            return;
        }
        int result = info.getSize(inItem);
        inner.putInt("amount", MathHelper.clamp(current + result, 0, 100));
        inner.putString("translation_key", info.getDriveTranslationKey(inItem));
        inner.putInt("drive_type", info.getDriveType(inItem).ordinal());
        nbt.put(key, inner);

        drive.getOrCreateTagElement(ProjectNublar.MODID).put("drive_information", nbt);
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
        String getDriveTranslationKey(ItemStack stack);
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
