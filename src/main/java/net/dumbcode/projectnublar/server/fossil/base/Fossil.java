package net.dumbcode.projectnublar.server.fossil.base;

import net.minecraft.util.ResourceLocation;

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

    public Fossil(double timeStart, double timeEnd, List<StoneType> stoneTypes, ResourceLocation texture, String name, boolean appendFossil) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.stoneTypes = stoneTypes;
        this.texture = texture;
        this.name = name;
        this.appendFossil = appendFossil;
    }
}
