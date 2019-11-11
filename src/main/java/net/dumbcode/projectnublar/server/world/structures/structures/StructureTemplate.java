package net.dumbcode.projectnublar.server.world.structures.structures;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.NBTTemplate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.PlacementSettings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
        return new Instance(world, pos, this.children, new PlacementSettings().setRotation(Rotation.values()[random.nextInt(Rotation.values().length)]));
    }

    @Nullable
    @Override
    public BlockPos attemptSize() {
        return this.template.getMaximum().subtract(this.template.getMinimum());
    }

    private class Instance extends StructureInstance {

        private final PlacementSettings settings;

        public Instance(World world, BlockPos position, int children, PlacementSettings settings) {
            super(world, position, children, template.getSizeX(), template.getSizeZ());
            this.settings = settings;
        }

        @Override
        public void build(Random random, List<DataHandler> handlers) {
            Biome biome = this.world.getBiome(this.position);
            StructureTemplate.this.template.addBlocksToWorld(this.world, this.position.add(-this.xSize/4, 0, -this.zSize/4), this, handlers, random, (pos, blockInfo) -> new NBTTemplate.BlockInfo(blockInfo.pos, BlockUtils.getBiomeDependantState(blockInfo.blockState, biome), blockInfo.tileentityData), this.settings, 2);
            for (DataHandler handler : handlers) {
                handler.end(DataHandler.Scope.STRUCTURE);
            }
        }

        @Override
        public boolean canBuild() { //todo: more predicates, and make the constants not so constant
            BlockPos minp = template.transformedBlockPos(this.settings, template.getMinimum());
            BlockPos maxp = template.transformedBlockPos(this.settings, template.getMaximum());

            double[] data = new double[(Math.abs(maxp.getX() - minp.getX()) + 1)*(Math.abs(maxp.getZ() - minp.getZ()) + 1)];
            AtomicInteger pointer = new AtomicInteger();

            AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);
            AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);

            AtomicInteger liquids = new AtomicInteger();
            AtomicInteger solids = new AtomicInteger();


            this.traverseTopdown(minp.getX(), maxp.getX(), minp.getZ(), maxp.getZ(), blockpos -> {
                data[pointer.getAndIncrement()] = blockpos.getY();
                max.getAndAccumulate(blockpos.getY(), Math::max);
                min.getAndAccumulate(blockpos.getY(), Math::min);
                if(this.world.getBlockState(blockpos.down()).getMaterial().isLiquid()) {
                    liquids.incrementAndGet();
                } else {
                    solids.incrementAndGet();
                }
            });


            if(liquids.floatValue() / (solids.get() + liquids.get()) > 0.3) { //cant be 30% water base
                return false;
            }
            if(max.get() - min.get() > 5) {
                return false;
            }
            return MathUtils.meanDeviation(data) <= 2;
        }
    }
}
