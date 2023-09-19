package net.dumbcode.projectnublar.server.fossil.base;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

public class Fossil {
    public double timeStart;
    public double timeEnd;
    @Nullable
    public List<StoneType> stoneTypes;
    public ResourceLocation texture;
    public String name;
    public boolean appendFossil;
    @Nullable
    public RegistryObject<Dinosaur> dinosaur;
    public String partName;
    public ResourceLocation itemTexture;

    public Fossil(double timeStart, double timeEnd, @Nullable List<StoneType> stoneTypes, ResourceLocation texture, String name, boolean appendFossil, @Nullable RegistryObject<Dinosaur> dinosaur, String partName, ResourceLocation itemTexture) {
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
}
