package net.dumbcode.projectnublar.server.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static net.minecraft.state.properties.BlockStateProperties.*;

@UtilityClass
public class BlockUtils {

    /**
     * Block should be plains specific.
     * TODO: re-evaluate all of this.
     */
    public static BlockState getBiomeDependantState(BlockState state, @Nullable Biome biome) {
        RegistryKey<Biome> key = ForgeRegistries.BIOMES.getEntries().stream()
            .filter(k -> k.getValue() == biome)
            .map(Map.Entry::getKey)
            .findFirst().orElse(Biomes.PLAINS);

        if(key == Biomes.PLAINS || biome == null) {
            return state;
        }

        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);

        for (BiomeDictionary.Type type : types) {
            if(type == BiomeDictionary.Type.MESA || biome.getBiomeCategory() == Biome.Category.MESA) {
                if(state.getBlock() == Blocks.GRASS) {
                    return Blocks.BROWN_TERRACOTTA.defaultBlockState();
                }
                if(state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.RED_SANDSTONE.defaultBlockState();
                }
                if(state.is(Tags.Blocks.STONE)) {
                    return Blocks.TERRACOTTA.defaultBlockState();
                }
                if(state.getBlock() == Blocks.ANDESITE) {
                    return Blocks.RED_TERRACOTTA.defaultBlockState();
                }
                if(state.getBlock() == Blocks.DIORITE) {
                    return Blocks.WHITE_TERRACOTTA.defaultBlockState();
                }
                if(state.getBlock() == Blocks.GRANITE) {
                    return Blocks.GRAY_TERRACOTTA.defaultBlockState();
                }
                if(state.getBlock() == Blocks.COAL_BLOCK) {
                    return Blocks.RED_SAND.defaultBlockState();
                }
                if (state.is(BlockTags.FENCES)) {
                    return Blocks.ACACIA_FENCE.defaultBlockState()
                        .setValue(NORTH, state.getValue(NORTH))
                        .setValue(EAST, state.getValue(EAST))
                        .setValue(SOUTH, state.getValue(SOUTH))
                        .setValue(WEST, state.getValue(WEST))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
                if (state.is(BlockTags.LOGS) || state.is(Tags.Blocks.STONE) || state.getBlock() == Blocks.GRASS_PATH) {
                    return Blocks.RED_SANDSTONE.defaultBlockState();
                }
                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.RED_SANDSTONE.defaultBlockState();
                }
                if (state.is(BlockTags.PLANKS)) {
                    return Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
                }
                if (state.is(BlockTags.STAIRS)) {
                    return Blocks.RED_SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(HALF, state.getValue(HALF))
                        .setValue(STAIRS_SHAPE, state.getValue(STAIRS_SHAPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
                if (state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.GRASS || state.is(Tags.Blocks.DIRT)) {
                    return Blocks.RED_SAND.defaultBlockState();
                }
            }
            if(type == BiomeDictionary.Type.SANDY || biome.getBiomeCategory() == Biome.Category.DESERT) {
                if (state.is(BlockTags.LOGS) || state.is(Tags.Blocks.STONE) || state.getBlock() == Blocks.GRASS_PATH) {
                    return Blocks.SANDSTONE.defaultBlockState();
                }
                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.SANDSTONE.defaultBlockState();
                }
                if (state.is(BlockTags.PLANKS)) {
                    return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
                }
                if (state.is(BlockTags.STAIRS)) {
                    return Blocks.SANDSTONE_STAIRS.defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(HALF, state.getValue(HALF))
                        .setValue(STAIRS_SHAPE, state.getValue(STAIRS_SHAPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
                if (state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.GRASS || state.is(Tags.Blocks.DIRT)) {
                    return Blocks.SAND.defaultBlockState();
                }
            }
            if(type == BiomeDictionary.Type.CONIFEROUS || biome.getBiomeCategory() == Biome.Category.TAIGA) {
                if (state.is(BlockTags.LOGS)) {
                    return Blocks.SPRUCE_LOG.defaultBlockState().setValue(AXIS, state.getValue(AXIS));
                }
                if (state.is(BlockTags.PLANKS)) {
                    return Blocks.SPRUCE_PLANKS.defaultBlockState();
                }
                if (state.is(BlockTags.STAIRS)) {
                    return Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(HALF, state.getValue(HALF))
                        .setValue(STAIRS_SHAPE, state.getValue(STAIRS_SHAPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
                if (state.is(BlockTags.FENCES)) {
                    return Blocks.SPRUCE_FENCE.defaultBlockState()
                        .setValue(NORTH, state.getValue(NORTH))
                        .setValue(EAST, state.getValue(EAST))
                        .setValue(SOUTH, state.getValue(SOUTH))
                        .setValue(WEST, state.getValue(WEST))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
            }
            if(type == BiomeDictionary.Type.SAVANNA || biome.getBiomeCategory() == Biome.Category.SAVANNA) {
                if (state.is(BlockTags.LOGS)) {
                    return Blocks.ACACIA_LOG.defaultBlockState().setValue(AXIS, state.getValue(AXIS));
                }

                if (state.is(BlockTags.PLANKS)) {
                    return Blocks.ACACIA_PLANKS.defaultBlockState();
                }

                if (state.is(BlockTags.STAIRS)) {
                    return Blocks.ACACIA_STAIRS.defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(HALF, state.getValue(HALF))
                        .setValue(STAIRS_SHAPE, state.getValue(STAIRS_SHAPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }
                if (state.is(BlockTags.FENCES)) {
                    return Blocks.ACACIA_FENCE.defaultBlockState()
                        .setValue(NORTH, state.getValue(NORTH))
                        .setValue(EAST, state.getValue(EAST))
                        .setValue(SOUTH, state.getValue(SOUTH))
                        .setValue(WEST, state.getValue(WEST))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
                }

                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.ACACIA_LOG.defaultBlockState().setValue(AXIS, Direction.Axis.Y);
                }
            }

        }


        return state;
    }

    public static BlockPos getTopSolid(World world, BlockPos pos){
        while (pos.getY() > 0 && !Block.isFaceFull(world.getBlockState(pos).getCollisionShape(world, pos), Direction.UP)) {
            pos = pos.below();
        }
        return pos;
    }

}
