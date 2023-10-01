package net.dumbcode.projectnublar.server.utils;

import net.minecraft.util.RegistryKey;
import net.minecraft.core.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BiomeUtils {

    public static int getBiomeColor(Biome biome) {
        RegistryKey<Biome> key = BiomeRegistry.byId(((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(biome.getRegistryName()));

        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
        if(types.contains(BiomeDictionary.Type.OCEAN)) {
            return biome.getSpecialEffects().getWaterColor(); //Water color
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
        int grassColor = biome.getSpecialEffects().getGrassColorOverride().orElse(0x48CD33);

        return
            ((int) (((grassColor >> 16) & 255) * modifier) << 16) +
                ((int) (((grassColor >> 8) & 255) * modifier) << 8) +
                (int) ((grassColor & 255) * modifier);
    }
}
