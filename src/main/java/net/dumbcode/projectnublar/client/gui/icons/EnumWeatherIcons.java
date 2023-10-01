package net.dumbcode.projectnublar.client.gui.icons;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.util.TriPredicate;

import java.util.function.BiPredicate;

@Getter
public enum EnumWeatherIcons implements WeatherIcon {
    DAY_NORMAL(999, (world, biome, pos) -> true),

    NIGHT_NORMAL(100, (world, biome, pos) -> world.getDayTime() % 24000 >= 12000),

    CLOUDY(90, (world, biome, pos) -> world.isRaining() || world.getRainLevel(1F) > 0F),

    RAIN_LIGHT(80, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN && world.isRaining()),
    RAIN_HEAVY(75, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN && world.isRaining() && world.getRainLevel(1F) > 0.75F),

    SNOW_LIGHT(70, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW && world.isRaining()),
    SNOW_HEAVY(65, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW && world.isRaining() && world.getRainLevel(1F) > 0.75F),

    THUNDERSTORM(50, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN && world.isThundering()),
    SNOWSTORM(40, (world, biome, pos) -> biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW && world.isThundering());

    private final float priority;
    private final ResourceLocation location = new ResourceLocation(ProjectNublar.MODID, "textures/gui/weather_icons.png");
    private final float[] UV;

    private final TriPredicate<Level, Biome, BlockPos> predicate;

    EnumWeatherIcons(float priority, TriPredicate<Level, Biome, BlockPos> predicate) {
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
    public boolean test(Level world, Biome biome, BlockPos pos) {
        return this.predicate.test(world, biome, pos);
    }


}
