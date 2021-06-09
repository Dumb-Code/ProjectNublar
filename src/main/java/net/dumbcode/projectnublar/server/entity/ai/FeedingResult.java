package net.dumbcode.projectnublar.server.entity.ai;

import lombok.Value;
import net.minecraft.nbt.CompoundNBT;

@Value
public class FeedingResult {
    private final int food;
    private final int water;

    public static CompoundNBT writeToNBT(CompoundNBT nbt, FeedingResult result) {
        nbt.putInt("food", result.food);
        nbt.putInt("water", result.water);
        return nbt;
    }

    public static FeedingResult readFromNbt(CompoundNBT nbt) {
        return new FeedingResult(nbt.getInt("food"), nbt.getInt("water"));
    }
}