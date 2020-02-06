package net.dumbcode.projectnublar.server.entity.ai;

import lombok.Value;
import net.minecraft.nbt.NBTTagCompound;

@Value
public class FeedingResult {
    private final int food;
    private final int water;

    public static NBTTagCompound writeToNBT(NBTTagCompound nbt, FeedingResult result) {
        nbt.setInteger("food", result.food);
        nbt.setInteger("water", result.water);
        return nbt;
    }

    public static FeedingResult readFromNbt(NBTTagCompound nbt) {
        return new FeedingResult(nbt.getInteger("food"), nbt.getInteger("water"));
    }
}