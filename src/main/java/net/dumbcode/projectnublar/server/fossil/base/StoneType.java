package net.dumbcode.projectnublar.server.fossil.base;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraftforge.common.util.Lazy;

public class StoneType {
    public double start;
    public double end;
    public int tint;
    public ResourceLocation sideTexture;
    public ResourceLocation bottomTexture;
    public ResourceLocation topTexture;
    public ResourceLocation particleTexture;
    public String name;
    /**
     * points to a tag. For example "mineable/pickaxe" would point to "mincraft:tags/mineable/pickaxe.json", while "example:mineable/randomname" would point to "example:tags/mineable/randomname.json"
     */
    public String mineableBy;
    public Material material;
    public float strength;
    public float blastStrength;
    public int harvestLevel;
    public RuleTest blocksToReplace;
    public Either<Lazy<Block>, Block> block;
    public int maxStrataSize;

    public StoneType(double start, double end, int tint, ResourceLocation sideTexture, ResourceLocation bottomTexture, ResourceLocation topTexture, ResourceLocation particleTexture, String name, String mineableBy, Material material, RuleTest blocksToReplace, Either<Lazy<Block>, Block> block, int maxStrataSize, int harvestLevel, float strength) {
        this.start = start;
        this.end = end;
        this.tint = tint;
        this.sideTexture = sideTexture;
        this.bottomTexture = bottomTexture;
        this.topTexture = topTexture;
        this.particleTexture = particleTexture;
        this.name = name;
        this.mineableBy = mineableBy;
        this.material = material;
        this.strength = strength;
        this.blastStrength = strength;
        this.harvestLevel = harvestLevel;
        this.blocksToReplace = blocksToReplace;
        this.block = block;
        this.maxStrataSize = maxStrataSize;
    }

    public StoneType(double start, double end, int tint, ResourceLocation sideTexture, ResourceLocation bottomTexture, ResourceLocation topTexture, ResourceLocation particleTexture, String name, String mineableBy, Material material, RuleTest blocksToReplace, Either<Lazy<Block>, Block> block, int maxStrataSize, int harvestLevel, float strength, float blastStrength) {
        this.start = start;
        this.end = end;
        this.tint = tint;
        this.sideTexture = sideTexture;
        this.bottomTexture = bottomTexture;
        this.topTexture = topTexture;
        this.particleTexture = particleTexture;
        this.name = name;
        this.mineableBy = mineableBy;
        this.material = material;
        this.strength = strength;
        this.blastStrength = blastStrength;
        this.harvestLevel = harvestLevel;
        this.blocksToReplace = blocksToReplace;
        this.block = block;
        this.maxStrataSize = maxStrataSize;
    }
}
