package net.dumbcode.projectnublar.server.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;
import java.util.Set;

@UtilityClass
public class BlockUtils {

    /**
     * Block should be plains specific
     */
    public static IBlockState getBiomeDependantState(IBlockState state, @Nullable Biome biome) {
        BiomeEvent.GetVillageBlockID event = new BiomeEvent.GetVillageBlockID(biome, state);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) {
            return event.getReplacement();
        }
        if(biome == Biomes.PLAINS || biome == null) {
            return state;
        }

        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);

        for (BiomeDictionary.Type type : types) {
            if(type == BiomeDictionary.Type.MESA || biome instanceof BiomeMesa) {
                if(state.getBlock() == Blocks.GRASS) {
                    return Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BROWN);
                }
                if(state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.RED_SANDSTONE.getDefaultState();
                }
                if(state.getBlock() == Blocks.STONE) {
                    BlockStone.EnumType value = state.getValue(BlockStone.VARIANT);
                    if(value == BlockStone.EnumType.ANDESITE) {
                        return Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED);
                    }
                    if(value == BlockStone.EnumType.DIORITE) {
                        return Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);
                    }
                    if(value == BlockStone.EnumType.GRANITE) {
                        return Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY);
                    }
                    return Blocks.HARDENED_CLAY.getDefaultState();

                }
                if(state.getBlock() == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT) {
                    return Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
                }
                if (state.getBlock() == Blocks.OAK_FENCE) {
                    return Blocks.ACACIA_FENCE.getDefaultState();
                }
            }
            if(type == BiomeDictionary.Type.MESA || biome instanceof BiomeMesa) {
                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2 || state.getBlock() == Blocks.STONE || state.getBlock() == Blocks.GRASS_PATH) {
                    return Blocks.RED_SANDSTONE.getDefaultState();
                }
                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.RED_SANDSTONE.getStateFromMeta(BlockRedSandstone.EnumType.DEFAULT.getMetadata());
                }
                if (state.getBlock() == Blocks.PLANKS) {
                    return Blocks.RED_SANDSTONE.getStateFromMeta(BlockRedSandstone.EnumType.SMOOTH.getMetadata());
                }
                if (state.getBlock() == Blocks.OAK_STAIRS || state.getBlock() == Blocks.STONE_STAIRS) {
                    return Blocks.RED_SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
                }
                if (state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT) {
                    return Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
                }
            }
            if(type == BiomeDictionary.Type.SANDY || biome instanceof BiomeDesert) {
                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2 || state.getBlock() == Blocks.STONE || state.getBlock() == Blocks.GRASS_PATH) {
                    return Blocks.SANDSTONE.getDefaultState();
                }
                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata());
                }
                if (state.getBlock() == Blocks.PLANKS) {
                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata());
                }
                if (state.getBlock() == Blocks.OAK_STAIRS || state.getBlock() == Blocks.STONE_STAIRS) {
                    return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
                }
                if (state.getBlock() == Blocks.GRAVEL || state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT) {
                    return Blocks.SAND.getDefaultState();
                }
            }
            if(type == BiomeDictionary.Type.CONIFEROUS || biome instanceof BiomeTaiga) {
                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
                    return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
                }
                if (state.getBlock() == Blocks.PLANKS) {
                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
                }
                if (state.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
                }
                if (state.getBlock() == Blocks.OAK_FENCE) {
                    return Blocks.SPRUCE_FENCE.getDefaultState();
                }
            }
            if(type == BiomeDictionary.Type.SAVANNA || biome instanceof BiomeSavanna) {
                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
                }

                if (state.getBlock() == Blocks.PLANKS) {
                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA);
                }

                if (state.getBlock() == Blocks.OAK_STAIRS) {
                    return Blocks.ACACIA_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
                }

                if (state.getBlock() == Blocks.COBBLESTONE) {
                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
                }

                if (state.getBlock() == Blocks.OAK_FENCE) {
                    return Blocks.ACACIA_FENCE.getDefaultState();
                }
            }

        }


        return state;
    }

    public static BlockPos getTopSolid(World world, BlockPos pos){
        while (pos.getY() > 0 && !world.isSideSolid(pos.down(), EnumFacing.UP)) {
            pos = pos.down();
        }
        return pos;
    }

}
