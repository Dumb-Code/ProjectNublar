package net.dumbcode.projectnublar.server.world.structures.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.predicates.StructurePredicate;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.vecmath.Vector2f;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class StructureNetwork {

    private final List<BuilderNode.Entry<Structure>> rootNodes;

    private final List<DataHandler> data;
    private final Map<String, List<StructurePredicate>> globalPredicates;
    private final StructureConstants constants;

    public Stats generate(World world, BlockPos pos, Random random) {
        long startTime = System.currentTimeMillis();

        StructureConstants.Decision constantDecision = this.constants.createDesicion(random);

        Set<BlockPos> pathPositions = Sets.newHashSet();
        List<Runnable> generations = Lists.newArrayList();

        BuilderNode.Entry<Structure> rootEntry = getWeightedChoice(random, this.rootNodes);
        StructureInstance instance = this.instantiate(world, pos, random, rootEntry);

        prepGeneration(world, pos, random, Sets.newHashSet(), instance, this.chooseChildren(random, rootEntry, instance), pathPositions, generations, constantDecision,0, 0);
        long prepTime = System.currentTimeMillis();

        this.generatePaths(world, pos, random, pathPositions);
        long pathTime = System.currentTimeMillis();

        generations.forEach(Runnable::run);
        for (DataHandler handler : this.data) {
            handler.end(DataHandler.Scope.NETWORK);
        }

        long generationTime = System.currentTimeMillis();

        return new Stats(
            prepTime - startTime,
            pathTime - prepTime,
            generationTime - pathTime,
            generations.size()
        );
    }

    private void generatePaths(World world, BlockPos startingPosition, Random random, Set<BlockPos> pathPositions) {
        Set<BlockPos> generatedPaths = Sets.newHashSet();
        Biome biome = world.getBiome(startingPosition);
        for (BlockPos pathPosition : pathPositions) {
            for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
                BlockPos pos = pathPosition.offset(horizontal);
                if(!pathPositions.contains(pos) && !generatedPaths.contains(pos)) {
                    generatedPaths.add(pos);
                    float perc = random.nextFloat();
                    pos = WorldUtils.getDirectTopdownBlock(world, pos.add(startingPosition)).down();
                    if(world.getBlockState(pos).getMaterial().isLiquid()) {
                        world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.PLANKS.getDefaultState(), biome));
                    } else if(perc < 0.6) {
                        world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                    }
                }
            }
            pathPosition = WorldUtils.getDirectTopdownBlock(world, pathPosition.add(startingPosition)).down();
            IBlockState setState = world.getBlockState(pathPosition).getMaterial().isLiquid() ? Blocks.PLANKS.getDefaultState() : Blocks.GRASS_PATH.getDefaultState();
            world.setBlockState(pathPosition, BlockUtils.getBiomeDependantState(setState, biome));
        }
    }

    private StructureInstance instantiate(World world, BlockPos pos, Random random, BuilderNode.Entry<Structure> entry) {
        StructureInstance instance = entry.getElement().createInstance(world, pos, random);
        instance.getGlobalPredicates().stream().filter(this.globalPredicates::containsKey).map(this.globalPredicates::get).forEach(instance.getPredicates()::addAll);
        return instance;
    }

    private void prepGeneration (
        World world,
        BlockPos pos,
        Random random,
        Set<Vector2f> placedEntries,
        StructureInstance instance,
        List<BuilderNode.Entry<Structure>> children,
        Set<BlockPos> pathPositions,
        List<Runnable> generations,
        StructureConstants.Decision decision,
        int baseoffX,
        int baseoffZ
    ) {
        if(!instance.canBuild()) {
            return;
        }
        generateEntry(instance, random, placedEntries, baseoffX, baseoffZ, generations, decision);
        for (BuilderNode.Entry<Structure> child : children) {
            BlockPos attemptSize = child.getElement().attemptSize();

            double startOffX = (instance.getXSize() + (attemptSize != null ? attemptSize.getX() : 0)) * 3F/4F;
            double startOffZ = (instance.getZSize() + (attemptSize != null ? attemptSize.getZ() : 0)) * 3F/4F;

            for (int tries = 0; tries < (attemptSize == null ? 25: 50); tries++) {
                double theta = 2F * Math.PI * random.nextDouble();

                int offx = (int) (Math.sin(theta) * (startOffX + tries / 2D) + baseoffX);
                int offz = (int) (Math.cos(theta) * (startOffZ + tries / 2D) + baseoffZ);


                StructureInstance childInstance = this.instantiate(world, pos.add(offx, 0, offz), random, child);

                if(this.doesStructureIntersect(childInstance, offx, offz, placedEntries) || !childInstance.canBuild()) {
                    continue;
                }

                prepGeneration(world, pos, random, placedEntries, childInstance, chooseChildren(random, child, childInstance), pathPositions, generations, decision, offx, offz);

                this.generatePath(pathPositions, baseoffX, baseoffZ, offx, offz, random);

                break;
            }
        }
    }

    private boolean doesStructureIntersect(StructureInstance instance, int offx, int offz, Set<Vector2f> placedEntries) {
        int padding = 5;
        for (int x = -padding; x < instance.getXSize()+padding; x++) {
            for (int z = -padding; z < instance.getZSize()+padding; z++) {
                if(placedEntries.contains(new Vector2f(offx+x, offz+z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generatePath(Set<BlockPos> pathPositions, int baseoffX, int baseoffZ, int offx, int offz, Random random) {
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
            double x = MathUtils.binomialExp(1-inc, inc, xints);
            double z = MathUtils.binomialExp(1-inc, inc, zints);
            pathPositions.add(new BlockPos(x, -1, z));
        }
    }

    private void generateEntry(StructureInstance structure, Random random, Set<Vector2f> placedEntries, int offX, int offZ, List<Runnable> generations, StructureConstants.Decision decision) {
        //2 blocks padding
        int padding = 2;
        //todo: have this as a function on the structure, as to make it more dynamic
        for (int x = -padding; x < structure.getXSize()+padding; x++) {
            for (int z = -padding; z < structure.getZSize()+padding; z++) {
                placedEntries.add(new Vector2f(offX+x, offZ+z));
            }
        }
        Random constRand = new Random(random.nextLong());
        generations.add(() -> structure.build(constRand, this.data, decision));
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

    private BuilderNode.Entry<Structure> getWeightedChoice(Random random, List<BuilderNode.Entry<Structure>> nodes) {
        int total = 0;
        for (BuilderNode.Entry<Structure> entry : nodes) {
            total += entry.getElement().getWeight();
        }

        int selected = random.nextInt(total);
        for (BuilderNode.Entry<Structure> entry : nodes) {
            selected -= entry.getElement().getWeight();
            if(selected <= 0) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Should not be accessible. Is the passed list empty?");

    }

    @Value
    public class Stats {
        private final long timeTakenPrepareMs;
        private final long timeTakenPathGeneration;
        private final long timeTakenGenerateMs;

        private final int structures;

    }

}
