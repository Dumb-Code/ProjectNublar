package net.dumbcode.projectnublar.server.fossil;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class Fossil {
    double timeStart;
    double timeEnd;
    @Nullable
    List<StoneType> stoneTypes;
    ResourceLocation texture;
    String name;

    public Fossil(double timeStart, double timeEnd, List<StoneType> stoneTypes, ResourceLocation texture, String name) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.stoneTypes = stoneTypes;
        this.texture = texture;
        this.name = name;
    }
}
