package net.dumbcode.projectnublar.server.fossil.base;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.AnnotationUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
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

    // TODO get the item texture from the registry name?
    // 0.0, "fossilised",
    // 0.5, "fresh",
    // 0.7, "amazing"
    // means:
    //   - 0.0 - 0.5 is "fossilised",
    //   - 0.5 - 0.7 is "fresh",
    //   - 0.7 - 1.0 is "amazing"
    //
    // See: getTextureForDNAValue
    // The list HAS to be ordered correctly (as above)
    private final List<Pair<Double, ResourceLocation>> textures = new ArrayList<>();

    @Nonnull
    public Collection<StoneType> getStoneTypes() {
        if(this.stoneTypes == null) {
            return StoneTypeHandler.STONE_TYPE_REGISTRY.get().getValues();
        }
        return stoneTypes.stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    public Fossil withTexture(double dnaValue, ResourceLocation texture) {
        textures.add(Pair.of(dnaValue, texture));
        return this;
    }

    @SafeVarargs
    public final Fossil withTextures(Pair<Double, ResourceLocation>... textures) {
        this.textures.addAll(Lists.newArrayList(textures));
        return this;
    }

    public ResourceLocation getTextureForDNAValue(double dnaValue) {
        ResourceLocation result = this.textures.get(0).getSecond();
        for (Pair<Double, ResourceLocation> texture : this.textures) {
            if (dnaValue < texture.getFirst()) {
                break;
            }
            result = texture.getSecond();
        }
        if (result == null) {
            throw new NullPointerException("Result cannot be null. Are there any textures added at all for " + this.getRegistryName() + "?");
        }
        return result;
    }

    public Collection<ResourceLocation> allTextures() {
        return this.textures.stream().map(Pair::getSecond).collect(Collectors.toList());
    }
}
