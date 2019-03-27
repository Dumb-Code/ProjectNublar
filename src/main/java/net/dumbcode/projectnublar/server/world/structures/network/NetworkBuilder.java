package net.dumbcode.projectnublar.server.world.structures.network;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class NetworkBuilder {
    private final World world;
    private final BlockPos startingPosition;

    public NetworkBuilder(World world, BlockPos startingPosition) {
        this.world = world;
        this.startingPosition = startingPosition;
    }

    public void generate(Random random, List<BuilderNode.Entry<Structure>> aviliable) {
        BuilderNode.Entry<Structure> rootEntry = getStructure(random, getWeightedChoice(random, aviliable), Lists.newArrayList()).get(0);
        List<Pair<Integer, Integer>> placedEntries = Lists.newArrayList();
        generateChildren(random, placedEntries, rootEntry, 0, 0);
    }

    private void generateChildren(Random random, List<Pair<Integer, Integer>> placedEntries, BuilderNode.Entry<Structure> entry, int baseoffX, int baseoffZ) {
        generateEntry(entry.getElement(), baseoffX, baseoffZ);
        for (BuilderNode.Entry<Structure> child : entry.getChildren()) {
            List<Pair<Integer, Integer>> places = Lists.newArrayList();
            outer:
            for (int tries = 0; tries < 100; tries++) {
                int offx = random.nextInt(entry.getElement().getXSize()) - entry.getElement().getXSize()/2;
                int offz = random.nextInt(entry.getElement().getZSize()) - entry.getElement().getZSize()/2;

                offx += baseoffX + 15 * Math.signum(offx);
                offz += baseoffZ + 15 * Math.signum(offz);

                for (int x = 0; x < child.getElement().getXSize(); x++) {
                    for (int z = 0; z < child.getElement().getZSize(); z++) {
                        places.add(Pair.of(offx-child.getElement().getXSize()/2+x, offz-child.getElement().getZSize()/2+z));
                    }
                }
                for (Pair<Integer, Integer> placedEntry : placedEntries) {
                    for (Pair<Integer, Integer> place : places) {
                        if(placedEntry.getLeft().equals(place.getLeft()) && placedEntry.getRight().equals(place.getRight())) {
                            continue outer;
                        }
                    }
                }
                placedEntries.addAll(places);
                generateChildren(random, placedEntries, child, offx, offz);

                int mpx = (baseoffX + offx) / 2;
                int mpz = (baseoffZ + offz) / 2;

                double theta = Math.atan2(baseoffZ - offz, baseoffX - offx) - Math.PI / 2;

                double offset = (random.nextInt(5) + 5) * Math.signum(random.nextInt(2)-1);

                int c1x = (int) (mpx + offset * Math.cos(theta)) + random.nextInt(4)-2;
                int c1z = (int) (mpz + offset * Math.sin(theta)) + random.nextInt(4)-2;

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

                    this.world.setBlockState(this.startingPosition.add(x, 0, z).up(2), Blocks.WOOL.getDefaultState());
                }

                break;
            }
        }
    }

    private void generateEntry(Structure structure, int offX, int offZ) {
        //todo:
        BlockPos base = this.startingPosition.add(offX, 0, offZ).add(-structure.getXSize()/2, 0, -structure.getZSize()/2);

        EnumDyeColor color = EnumDyeColor.values()[new Random().nextInt(EnumDyeColor.values().length)];

        for (int x = 0; x < structure.getXSize(); x++) {
            for (int z = 0; z < structure.getXSize(); z++) {
                this.world.setBlockState(base.add(x, 0, z), Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, color));
            }
        }

    }

    private List<BuilderNode.Entry<Structure>> getStructure(Random random, BuilderNode.Entry<Structure> entry, List<BuilderNode.Entry<Structure>> generatedStructures) {
        BuilderNode.Entry<Structure> node = new BuilderNode.Entry<>(entry.getElement());
        generatedStructures.add(node);
        if(!entry.getChildren().isEmpty()) {
            //random.nextGaussian has a standard deviation of 1, meaning there is:
            // - 70% chance for an additional 0 children
            // - 27% for an additional +-1 child
            // - 4% for an additional +-2 children
            // - 0.26% for an additional +-3 children
            //After that is essentially is 0%
            int childrenAmount = MathUtils.floorToZero(random.nextGaussian()) + entry.getElement().getChildren();
            for (int i = 0; i < childrenAmount; i++) {
                getStructure(random, getWeightedChoice(random, entry.getChildren()), node.getChildren());
            }
        }
        return generatedStructures;
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
