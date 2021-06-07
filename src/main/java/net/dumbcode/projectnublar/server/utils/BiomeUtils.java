package net.dumbcode.projectnublar.server.utils;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BiomeUtils {

    public static int getBiomeColor(Biome biome) {
        RegistryKey<Biome> key = ForgeRegistries.BIOMES.getEntries().stream()
            .filter(k -> k.getValue() == biome)
            .map(Map.Entry::getKey)
            .findFirst().orElse(Biomes.PLAINS);


        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
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

        return getGrassColor(biome, 0.5F);
    }

    public static int getGrassColor(Biome biome, float modifier) {
        int grassColor = biome.getSpecialEffects().getGrassColorOverride().orElse(4159204);

        return
            ((int) (((grassColor >> 16) & 255) * modifier) << 16) +
                ((int) (((grassColor >> 8) & 255) * modifier) << 8) +
                (int) ((grassColor & 255) * modifier);
    }
}
