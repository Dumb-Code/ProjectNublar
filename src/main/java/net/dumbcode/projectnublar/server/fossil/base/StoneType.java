package net.dumbcode.projectnublar.server.fossil.base;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class StoneType {
    public double start;
    public double end;
    public int tint;
    public ResourceLocation texture;
    public String name;
    /**
     * points to a tag. For example "mineable/pickaxe" would point to "mincraft:tags/mineable/pickaxe.json", while "example:mineable/randomname" would point to "example:tags/mineable/randomname.json"
     */
    public String mineableBy;
    public Material material;
    public float strength;
    public float blastStrength;
    public int harvestLevel;

    public StoneType(double start, double end, int tint, ResourceLocation texture, String name, String mineableBy, Material material, int harvestLevel, float strength) {
        this.start = start;
        this.end = end;
        this.tint = tint;
        this.texture = texture;
        this.name = name;
        this.mineableBy = mineableBy;
        this.material = material;
        this.strength = strength;
        this.blastStrength = strength;
        this.harvestLevel = harvestLevel;
    }

    public StoneType(double start, double end, int tint, ResourceLocation texture, String name, String mineableBy, Material material, int harvestLevel, float strength, float blastStrength) {
        this.start = start;
        this.end = end;
        this.tint = tint;
        this.texture = texture;
        this.name = name;
        this.mineableBy = mineableBy;
        this.material = material;
        this.strength = strength;
        this.blastStrength = blastStrength;
        this.harvestLevel = harvestLevel;
    }
}
