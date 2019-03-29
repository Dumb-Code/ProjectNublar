package net.dumbcode.projectnublar.server.world.structures.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.block.BlockDirt;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class NetworkBuilder {
    private final World world;
    private final BlockPos startingPosition;
    private Set<BlockPos> pathPositions = Sets.newHashSet();

    public NetworkBuilder(World world, BlockPos startingPosition) {
        this.world = world;
        this.startingPosition = world.getTopSolidOrLiquidBlock(startingPosition);
    }

    public void generate(Random random, List<BuilderNode.Entry<Structure>> aviliable) {
        BuilderNode.Entry<Structure> rootEntry = getWeightedChoice(random, aviliable);
        List<Pair<Integer, Integer>> placedEntries = Lists.newArrayList();
        StructureInstance instance = rootEntry.getElement().createInstance(this.world, this.startingPosition, random);
        generateChildren(random, placedEntries, instance, chooseChildren(random, rootEntry, instance), 0, 0);

        Set<BlockPos> generatedPaths = Sets.newHashSet();
        Biome biome = this.world.getBiome(this.startingPosition);
        for (BlockPos pathPosition : this.pathPositions) {
            if(!placedEntries.contains(Pair.of(pathPosition.getX(), pathPosition.getZ()))) {
                for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
                    BlockPos pos = pathPosition.offset(horizontal);
                    if(!this.pathPositions.contains(pos) && !generatedPaths.contains(pos)) {
                        generatedPaths.add(pos);
                        float perc = random.nextFloat();
                        pos = this.world.getTopSolidOrLiquidBlock(pos.add(this.startingPosition)).down();
                        if(perc < 0.3) {
                            this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                        } else if (perc < 0.6) {
                            this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                        }
                    }
                }
                pathPosition = this.world.getTopSolidOrLiquidBlock(pathPosition.add(this.startingPosition)).down();
                this.world.setBlockState(pathPosition, BlockUtils.getBiomeDependantState(Blocks.GRASS_PATH.getDefaultState(), biome));
            }
        }
    }

    private void generateChildren(Random random, List<Pair<Integer, Integer>> placedEntries, StructureInstance instance, List<BuilderNode.Entry<Structure>> children, int baseoffX, int baseoffZ) {
        if(!instance.canBuild()) {
            return;
        }
        generateEntry(instance, random, placedEntries, baseoffX, baseoffZ);
        for (BuilderNode.Entry<Structure> child : children) {
            List<Pair<Integer, Integer>> places = Lists.newArrayList();
            outer:
            for (int tries = 0; tries < 100; tries++) {

                int offx = random.nextInt(instance.getXSize()) - instance.getXSize()/2;
                int offz = random.nextInt(instance.getZSize()) - instance.getZSize()/2;

                offx += baseoffX + 15 * Math.signum(offx);
                offz += baseoffZ + 15 * Math.signum(offz);

                StructureInstance childInstance = child.getElement().createInstance(this.world, this.startingPosition.add(offx, 0, offz), random);
                if(!childInstance.canBuild()) {
                    continue;
                }
                for (int x = 0; x < childInstance.getXSize(); x++) {
                    for (int z = 0; z < childInstance.getZSize(); z++) {
                        places.add(Pair.of(offx-childInstance.getXSize()/2+x, offz-childInstance.getZSize()/2+z));
                    }
                }
                for (Pair<Integer, Integer> placedEntry : placedEntries) {
                    for (Pair<Integer, Integer> place : places) {
                        if(placedEntry.getLeft().equals(place.getLeft()) && placedEntry.getRight().equals(place.getRight())) {
                            continue outer;
                        }
                    }
                }

                generateChildren(random, placedEntries, childInstance, chooseChildren(random, child, childInstance), offx, offz);

                int mpx = (baseoffX + offx) / 2;
                int mpz = (baseoffZ + offz) / 2;

                double theta = Math.atan2(baseoffZ - offz, baseoffX - offx) - Math.PI / 2;

                double offset = (random.nextInt(5) + 5) * (random.nextBoolean()?1:-1);

                int c1x = (int) (mpx + offset * Math.cos(theta));
                int c1z = (int) (mpz + offset * Math.sin(theta));

                double res = 0.25D / Math.sqrt((baseoffX-offx)*(baseoffX-offx) + (baseoffZ-offz)*(baseoffZ-offz));

                int[] xpoints = new int[]{offx, c1x, baseoffX};
                int[] zpoints = new int[]{offz, c1z, baseoffZ};

                int[] ints = MathUtils.binChoose(xpoints.length - 1);

                int[] xints = new int[ints.length];
                int[] zints = new int[ints.length];

                for (int i = 0; i < ints.length; i++) {
                    xints[i] = ints[i] * xpoints[i];
                    zints[i] = ints[i] * zpoints[i];
                }

                for (double inc = 0; inc < 1D; inc += res) {
                    double x = MathUtils.binomial(1-inc, inc, xints);
                    double z = MathUtils.binomial(1-inc, inc, zints);
                    this.pathPositions.add(new BlockPos(x, -1, z));
                }

                break;
            }
        }
    }

    private void generateEntry(StructureInstance structure, Random random, List<Pair<Integer, Integer>> placedEntries, int offX, int offZ) {

        for (int x = 0; x < structure.getXSize(); x++) {
            for (int z = 0; z < structure.getZSize(); z++) {
                placedEntries.add(Pair.of(offX-structure.getXSize()/2+x, offZ-structure.getZSize()/2+z));
            }
        }

        structure.build(random);

//        todo:
//        BlockPos base = this.startingPosition.add(offX, 0, offZ).add(-structure.getXSize()/2, 0, -structure.getZSize()/2);
//
//        EnumDyeColor color = EnumDyeColor.values()[new Random().nextInt(EnumDyeColor.values().length)];
//
//        for (int x = 0; x < structure.getXSize(); x++) {
//            for (int z = 0; z < structure.getXSize(); z++) {
//                this.world.setBlockState(base.add(x, 0, z), Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, color));
//            }
//        }

    }

    private List<BuilderNode.Entry<Structure>> chooseChildren(Random random, BuilderNode.Entry<Structure> entry, StructureInstance instance) {
        List<BuilderNode.Entry<Structure>> children = Lists.newArrayList();
        if(!entry.getChildren().isEmpty()) {
            //random.nextGaussian has a standard deviation of 1, meaning there is:
            // - 70% chance for an additional 0 children
            // - 27% for an additional +-1 child
            // - 4% for an additional +-2 children
            // - 0.26% for an additional +-3 children
            //After that is essentially is 0%
            int childrenAmount = MathUtils.floorToZero(random.nextGaussian()) + instance.getChildren();
            for (int i = 0; i < childrenAmount; i++) {
                children.add(getWeightedChoice(random, entry.getChildren()));
            }
        }
        return children;
    }

    private BuilderNode.Entry<Structure> getWeightedChoice(Random random, List<BuilderNode.Entry<Structure>> aviliable) {
        int total = 0;
        for (BuilderNode.Entry<Structure> entry : aviliable) {
            total += entry.getElement().getWeight();
        }

        int selected = random.nextInt(total);
        for (BuilderNode.Entry<Structure> entry : aviliable) {
            selected -= entry.getElement().getWeight();
            if(selected <= 0) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Should not be accessible. Is the passed list empty?");

    }
}
