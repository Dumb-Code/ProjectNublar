package net.dumbcode.projectnublar.client.gui.icons;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.function.BiPredicate;

@Getter
public enum EnumWeatherIcons implements WeatherIcon {
    DAY_NORMAL(999, (world, biome) -> true),

    NIGHT_NORMAL(100, (world, biome) -> world.getWorldTime() % 24000 >= 12000),

    CLOUDY(90, (world, biome) -> world.isRaining() || world.getRainStrength(1F) > 0F),

    RAIN_LIGHT(80, (world, biome) -> biome.canRain() && world.isRaining()),
    RAIN_HEAVY(75, (world, biome) -> biome.canRain() && world.isRaining() && world.getRainStrength(1F) > 0.75F),

    SNOW_LIGHT(70, (world, biome) -> biome.getEnableSnow() && world.isRaining()),
    SNOW_HEAVY(65, (world, biome) -> biome.getEnableSnow() && world.isRaining() && world.getRainStrength(1F) > 0.75F),

    THUNDERSTORM(50, (world, biome) -> biome.canRain() && world.isThundering()),
    SNOWSTORM(40, (world, biome) -> biome.getEnableSnow() && world.isThundering());

    private final float priority;
    private final ResourceLocation location = new ResourceLocation(ProjectNublar.MODID, "textures/gui/weather_icons.png");
    private final float[] UV;

    private final BiPredicate<World, Biome> predicate;

    EnumWeatherIcons(float priority, BiPredicate<World, Biome> predicate) {
        this.priority = priority;
        this.predicate = predicate;

        float iconsPerPage = 3F;

        int xIndex = (int) (this.ordinal() % iconsPerPage);
        int yIndex = (int) (this.ordinal() / iconsPerPage);

        this.UV = new float[] {
            xIndex / iconsPerPage,
            yIndex / iconsPerPage,
            (xIndex + 1) / iconsPerPage,
            (yIndex + 1) / iconsPerPage
        };
    }

    public static void register() {
        for (EnumWeatherIcons value : values()) {
            WeatherIcon.registerIcon(value);
        }
    }

    @Override
    public boolean test(World world, Biome biome) {
        return this.predicate.test(world, biome);
    }


}
