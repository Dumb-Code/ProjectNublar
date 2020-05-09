package net.dumbcode.projectnublar.server.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Set;

public class BiomeUtils {

    public static int getBiomeColor(BlockPos pos, Biome biome) {
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
        if(types.contains(BiomeDictionary.Type.OCEAN)) {
            return 0x3737DC; //Water color
        }

        if(types.contains(BiomeDictionary.Type.RIVER)) {
            return 0x3a93e0; //Light blue water
        }

        if(types.contains(BiomeDictionary.Type.MESA)) {
            return 0xFF9033;
        }

        if(types.contains(BiomeDictionary.Type.SANDY)) {
            return 0xD5C98C;
        }

        return getGrassColor(biome, pos, 0.5F);
    }

    public static int getGrassColor(Biome biome, BlockPos pos, float modifier) {
        int grassColor = biome.getGrassColorAtPos(pos);

        return
            ((int) (((grassColor >> 16) & 255) * modifier) << 16) +
                ((int) (((grassColor >> 8) & 255) * modifier) << 8) +
                (int) ((grassColor & 255) * modifier);
    }
}
