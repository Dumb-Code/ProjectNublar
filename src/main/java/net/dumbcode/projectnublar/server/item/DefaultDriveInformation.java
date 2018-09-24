package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DefaultDriveInformation extends BasicDinosaurItem implements DriveUtils.DriveInformation {

    private final String key;
    private final int size;

    public DefaultDriveInformation(Dinosaur dinosaur, String key, int size) {
        super(dinosaur);
        this.key = key;
        this.size = size;
    }

    @Override
    public int getSize(ItemStack stack) {
        return this.size;
    }

    @Override
    public String getKey(ItemStack stack) {
        return this.key;
    }
}
