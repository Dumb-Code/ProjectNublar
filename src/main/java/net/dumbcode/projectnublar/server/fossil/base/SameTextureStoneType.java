package net.dumbcode.projectnublar.server.fossil.base;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.RuleTest;
import net.minecraftforge.common.util.Lazy;

public class SameTextureStoneType extends StoneType{

    public SameTextureStoneType(double start, double end, int tint, ResourceLocation texture, String name, String mineableBy, Material material, RuleTest blocksToReplace, Either<Lazy<Block>, Block> block, int maxStrataSize, int harvestLevel, float strength) {
        super(start, end, tint, texture, texture, texture, texture, name, mineableBy, material, blocksToReplace, block, maxStrataSize, harvestLevel, strength);
    }

    public SameTextureStoneType(double start, double end, int tint, ResourceLocation texture, String name, String mineableBy, Material material, RuleTest blocksToReplace, Either<Lazy<Block>, Block> block, int maxStrataSize, int harvestLevel, float strength, float blastStrength) {
        super(start, end, tint, texture, texture, texture, texture, name, mineableBy, material, blocksToReplace, block, maxStrataSize, harvestLevel, strength, blastStrength);
    }
}
