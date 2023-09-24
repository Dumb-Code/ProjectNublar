package net.dumbcode.projectnublar.server.fossil.base;

import lombok.Value;
import net.minecraft.block.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

@Value
public class StoneType extends ForgeRegistryEntry<StoneType> {
    public double start;
    public double end;
    // We can't use just a BlockState as when the StoneType is initialised
    // The Blocks may not be initialised
    public Supplier<BlockState> baseState;
    public int maxStrataSize;
}
