package net.dumbcode.projectnublar.server.fossil.base;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class Fossil extends ForgeRegistryEntry<Fossil> {

    public final double timeStart;
    public final double timeEnd;
    @Nullable
    private final List<RegistryObject<StoneType>> stoneTypes;
    public final String textureName;
    public final String name;
    @Nullable
    public final Supplier<Dinosaur> dinosaur;
    public final String partName;
    // TODO get the item texture from the registry name
    public final Map<Double, ResourceLocation> textures = new HashMap<>();

    @Nonnull
    public Collection<StoneType> getStoneTypes() {
        if(this.stoneTypes == null) {
            return StoneTypeHandler.STONE_TYPE_REGISTRY.get().getValues();
        }
        return stoneTypes.stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    public Fossil withTexture(double dnaValue, ResourceLocation texture) {
        textures.put(dnaValue, texture);
        return this;
    }

    @SafeVarargs
    public final Fossil withTextures(Pair<Double, ResourceLocation>... textures) {
        for (Pair<Double, ResourceLocation> texture : textures) {
            this.textures.put(texture.getFirst(), texture.getSecond());
        }
        return this;
    }
}
