package net.dumbcode.projectnublar.client.gui.icons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface WeatherIcon {
    List<WeatherIcon> ORDERED_ICONS = new ArrayList<>();

    static void registerIcon(WeatherIcon icon) {
        //todo: just insert at the right place, don't set and resort
        ORDERED_ICONS.add(icon);
        ORDERED_ICONS.sort(Comparator.comparing(WeatherIcon::getPriority));
    }

    static WeatherIcon guess(Level world, BlockPos pos) {
        Holder<Biome> biome = world.getBiome(pos);
        for (WeatherIcon icon : ORDERED_ICONS) {
            if (icon.test(world, biome.get(), pos)) {
                return icon;
            }
        }
        throw new IllegalStateException("Unable to match to any icons. Are you calling before they're registered?");
    }

    //Lowest = greater
    float getPriority();

    boolean test(Level world, Biome biome, BlockPos pos);

    ResourceLocation getLocation();
    //[minu, minv, maxu, maxv]
    float[] getUV();


}
