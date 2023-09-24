package net.dumbcode.projectnublar.server.fossil.base;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Fossil extends ForgeRegistryEntry<Fossil> {


    public final double timeStart;
    public final double timeEnd;
    @Nullable
    public final List<RegistryObject<StoneType>> stoneTypes;
    // TODO get the texture based on the registry name
    @Deprecated
    public final ResourceLocation texture;
    public final String name;
    public final boolean appendFossil;
    @Nullable
    public final Supplier<Dinosaur> dinosaur;
    public final String partName;
    // TODO get the item texture from the registry name
    @Deprecated
    public final ResourceLocation itemTexture;

}
