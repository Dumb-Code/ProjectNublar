package net.dumbcode.projectnublar.server.fossil.base;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class Fossil implements IForgeRegistryEntry<Fossil> {

    private ResourceLocation registryName;

    public final double timeStart;
    public final double timeEnd;
    @Nullable
    public final List<StoneType> stoneTypes;
    public final ResourceLocation texture;
    public final String name;
    public final boolean appendFossil;
    @Nullable
    public final Supplier<Dinosaur> dinosaur;
    public final String partName;
    public final ResourceLocation itemTexture;

    public Fossil(double timeStart, double timeEnd, @Nullable List<StoneType> stoneTypes, ResourceLocation texture, String name, boolean appendFossil, @Nullable Supplier<Dinosaur> dinosaur, String partName, ResourceLocation itemTexture) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.stoneTypes = stoneTypes;
        this.texture = texture;
        this.name = name;
        this.appendFossil = appendFossil;
        this.dinosaur = dinosaur;
        this.partName = partName;
        this.itemTexture = itemTexture;
    }

    @Override
    public Fossil setRegistryName(ResourceLocation name) {
        this.registryName = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @Override
    public Class<Fossil> getRegistryType() {
        return Fossil.class;
    }
}
