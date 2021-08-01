package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Locale;

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
        return getAmount(drive, key) < 100;
    }

    public static List<DriveEntry> getAll(ItemStack drive) {
        List<DriveEntry> out = Lists.newArrayList();
        ListNBT nbt = drive.getOrCreateTagElement(ProjectNublar.MODID).getList("drive_information", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT tag = nbt.getCompound(i);
            out.add(new DriveEntry(tag.getString("drive_key"), tag.getString("translation_key"), tag.contains("animal_variant") ? tag.getString("animal_variant") : null, tag.getInt("amount"), DriveType.values()[tag.getInt("drive_type") % DriveType.values().length]));
        }

        return out;
    }

    public static int getAmount(ItemStack drive, String key) {
        for (DriveEntry entry : getAll(drive)) {
            if(entry.getKey().equals(key)) {
                return entry.getAmount();
            }
        }
        return 0;
    }

    public static DriveType getType(ItemStack drive, String key) {
        for (DriveEntry entry : getAll(drive)) {
            if(entry.getKey().equals(key)) {
                return entry.getDriveType();
            }
        }
        return DriveType.OTHER; //??
    }

    public static void addItemToDrive(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();
        ListNBT list = drive.getOrCreateTagElement(ProjectNublar.MODID).getList("drive_information", Constants.NBT.TAG_COMPOUND);
        String key = info.getKey(inItem);
        String variant = info.getAnimalVariant(inItem);
        if(key.isEmpty()) {
            return;
        }

        int index = list.size();
        for (int i = 0; i < list.size(); i++) {
            if(list.getCompound(i).getString("drive_key").equals(key)) {
                index = i;
            }
        }
        CompoundNBT inner = list.getCompound(index);
        int current = inner.getInt("amount");
        if(current >= 100) {
            return;
        }
        int result = info.getSize(inItem);
        inner.putString("drive_key", key);
        inner.putInt("amount", MathHelper.clamp(current + result, 0, 100));
        inner.putString("translation_key", info.getDriveTranslationKey(inItem));
        if(variant != null) {
            inner.putString("animal_variant", variant);
        }
        inner.putInt("drive_type", info.getDriveType(inItem).ordinal());

        list.add(index, inner);

        drive.getOrCreateTagElement(ProjectNublar.MODID).put("drive_information", list);
    }

    public static TranslationTextComponent getTranslation(String name, String variant) {
        TranslationTextComponent component = new TranslationTextComponent(name);
        if(variant != null) {
            return ProjectNublar.translate("entity.genetics.variant." + variant.toLowerCase(Locale.ROOT), component);
        } else {
            return component;
        }
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
        String getDriveTranslationKey(ItemStack stack);
        default String getAnimalVariant(ItemStack stack) {
            return null;
        }
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

    @Value
    public static class DriveEntry {
        String key;
        String name;
        String variant;
        int amount;
        DriveType driveType;

        public TranslationTextComponent getTranslation() {
            return DriveUtils.getTranslation(this.name, this.variant);
        }
    }

    public enum DriveType {
        DINOSAUR,
        OTHER
    }

}
