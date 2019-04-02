package net.dumbcode.projectnublar.server.world.structures.structures;

import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;

import java.util.Random;

public class StructureTemplate extends Structure {

    private final NBTTemplate template;
    private final int children;


    public StructureTemplate(NBTTemplate template, int children, int weight) {
        super(weight);
        this.template = template;
        this.children = children;
    }


    @Override
    public StructureInstance createInstance(World world, BlockPos pos, Random random) {
        return new Instance(world, pos, this.children, this.template.getSize().getX(), this.template.getSize().getZ());
    }

    private class Instance extends StructureInstance {

        public Instance(World world, BlockPos position, int children, int xSize, int zSize) {
            super(world, position, children, xSize, zSize);
        }

        @Override
        public void build(Random random) {
            Biome biome = this.world.getBiome(this.position);
            StructureTemplate.this.template.addBlocksToWorld(this.world, this.position, (pos, blockInfo) -> new NBTTemplate.BlockInfo(blockInfo.pos, BlockUtils.getBiomeDependantState(blockInfo.blockState, biome), blockInfo.tileentityData), new PlacementSettings().setRotation(Rotation.values()[random.nextInt(Rotation.values().length)]), 2);
        }

        @Override
        public boolean canBuild() { //todo: more predicates, and make the constants not so constant
            double[] data = new double[this.xSize * this.zSize];
            int pointer = 0;

            float liquids = 0;
            float solids = 0;

            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;

            for (int x = -this.xSize/2; x < this.xSize/2; x++) {
                for (int z = -this.zSize/2; z < this.zSize/2; z++) {
                    BlockPos pos = this.position.add(x, 0, z);

                    Chunk chunk = this.world.getChunkFromBlockCoords(pos);
                    BlockPos blockpos;
                    BlockPos blockpos1;
                    for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
                        blockpos1 = blockpos.down();
                        IBlockState state = chunk.getBlockState(blockpos1);

                        if (state.getMaterial().isLiquid() || state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, this.world, blockpos1) && !state.getBlock().isFoliage(this.world, blockpos1)) {
                            break;
                        }
                    }


                    data[pointer++] = blockpos.getY();
                    max = Math.max(max, blockpos.getY());
                    min = Math.min(min, blockpos.getY());
                    if(this.world.getBlockState(blockpos.down()).getMaterial().isLiquid()) {
                        liquids++;
                    } else {
                        solids++;
                    }
                }
            }
            if(liquids / (solids + liquids) > 0.3) { //cant be 30% water base
                return false;
            }
            if(max - min > 5) {
                return false;
            }
            double diviation = MathUtils.meanDeviation(data);
            return diviation <= 2;
        }
    }
}
