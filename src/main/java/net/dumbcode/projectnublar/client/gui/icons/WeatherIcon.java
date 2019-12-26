package net.dumbcode.projectnublar.client.gui.icons;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface WeatherIcon {
    List<WeatherIcon> ORDERED_ICONS = new ArrayList<>();

    static void registerIcon(WeatherIcon icon) {
        //todo: just insert at the right place, don't add and resort
        ORDERED_ICONS.add(icon);
        ORDERED_ICONS.sort(Comparator.comparing(WeatherIcon::getPriority));
    }

    static WeatherIcon guess(World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        for (WeatherIcon icon : ORDERED_ICONS) {
            if (icon.test(world, biome)) {
                return icon;
            }
        }
        throw new IllegalStateException("Unable to match to any icons. Are you calling before they're registered?");
    }

    //Lowest = greater
    float getPriority();

    boolean test(World world, Biome biome);

    ResourceLocation getLocation();
    //[minu, minv, maxu, maxv]
    float[] getUV();


}
