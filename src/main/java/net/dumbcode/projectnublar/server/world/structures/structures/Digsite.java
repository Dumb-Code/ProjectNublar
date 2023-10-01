package net.dumbcode.projectnublar.server.world.structures.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.LootTableHandler;
import net.dumbcode.projectnublar.server.world.constants.ConstantDefinition;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Digsite extends Structure {

    public static final ConstantDefinition<Dinosaur> DIGSITE_DINOSAUR = new ConstantDefinition<>();

    private final int size;

    public Digsite(int weight, int size, int children) {
        super(weight, children);
        this.size = size;
    }

    @Override
    public StructureInstance createInstance(@Nullable StructureInstance parent, ServerWorld world, BlockPos pos, Random random) {
        int overallsize = (int) Math.abs(random.nextGaussian()) + this.size;
        int totalLayers = 2 + random.nextInt(2);
        Circle[][] circles = new Circle[totalLayers][];

        int minx = Integer.MAX_VALUE;
        int minz = Integer.MAX_VALUE;

        float maxx = Integer.MIN_VALUE;
        float maxz = Integer.MIN_VALUE;

        for (int layer = 0; layer < totalLayers; layer++) {
            float xsize = random.nextFloat() + 0.5F; // 0.5 -> 1.5
            float zsize = random.nextFloat() + 0.5F;
            int determinedSize = ((layer) + overallsize + 1);
            int numcircles = 5;//random.nextInt(2) +
            Circle[] layercircs = circles[layer] = new Circle[numcircles];
            for (int circle = 0; circle < numcircles; circle++) {
                int startx = 0;
                int startz = 0;
                if (circle > 0) {
                    startx += random.nextInt(determinedSize) - determinedSize/2;
                    startz += random.nextInt(determinedSize) - determinedSize/2;
                }
                double distx = Math.sqrt((determinedSize*determinedSize)/xsize);
                double distz = Math.sqrt((determinedSize*determinedSize)/zsize);

                minx = Math.min(minx, startx-MathHelper.floor(distx));
                minz = Math.min(minz, startz-MathHelper.floor(distz));

                maxx = Math.max(maxx, startx+MathHelper.ceil(distx));
                maxz = Math.max(maxz, startz+MathHelper.ceil(distz));
                layercircs[circle] = new Circle(startx, startz, determinedSize, xsize, zsize, distx, distz);
            }
        }

        return new Instance(parent, world, pos.offset(minx, 0, minz), overallsize, circles, totalLayers, new float[] {minx, maxx, minz, maxz}, this);
    }

    @AllArgsConstructor
    private class Circle {int startx, startz, rad; float xsize, zsize; double distx, distz;}

    public class Instance extends StructureInstance {

        private final int overallsize;
        private final Circle[][] circles;
        private final int totalLayers;
        private final BlockPos centralPosition;

        public Instance(@Nullable StructureInstance parent, ServerWorld world, BlockPos position, int overallsize, Circle[][] circles, int totalLayers, float[] dims, Digsite structure) {
            super(parent, world, position, (int) (dims[1] - dims[0]), (int) (dims[3] - dims[2]), structure);
            this.overallsize = overallsize;
            this.circles = circles;
            this.totalLayers = totalLayers;
            this.centralPosition =  BlockUtils.getTopSolid(world, new BlockPos(position.getX() - dims[0], 257, position.getZ() - dims[2]));
        }

        @Override
        public void build(Random random, List<DataHandler> handlers, StructureConstants.Decision decision) {
            Set<BlockPos> holePositions = Sets.newHashSet();
            Set<BlockPos> fossilPositions = Sets.newHashSet();

            for (int layer = 0; layer < this.circles.length; layer++) {
                for (Circle circle : this.circles[layer]) {
                    this.generateCircularHole(circle, layer, holePositions, fossilPositions, random);
                }
            }

            this.setFossils(fossilPositions, decision.requireEntry(DIGSITE_DINOSAUR));

            this.generateHoleDecor(holePositions, random);
        }

        private void generateHoleDecor(Set<BlockPos> holePositions, Random random) {
            Set<BlockPos> chests = Sets.newHashSet();
            Set<BlockPos> edgePositions = Sets.newHashSet();

            NonNullList<ItemStack> drops = NonNullList.create();
            Biome biome = this.world.getBiome(this.centralPosition);

            this.digOutHole(holePositions, edgePositions, random, drops, biome);

            for (BlockPos edge : edgePositions) {
                if(Block.isFaceFull(this.world.getBlockState(edge.below()).getShape(this.world, edge.below()), Direction.UP)) {
                    if (random.nextFloat() < 0.95) {
                        chests.add(edge);
                    } else {
                        this.world.setBlock(edge, BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.defaultBlockState(), biome), 2);
                        this.world.setBlock(edge.above(), BlockUtils.getBiomeDependantState(Blocks.TORCH.defaultBlockState(), biome), 2);
                    }
                }
                for (Direction facing : Direction.values()) {
                    if(facing.getAxis() == Direction.Axis.Y) {
                        continue;
                    }
                    BlockPos blockPos = edge.relative(facing.getOpposite());
                    if(Block.isFaceFull(this.world.getBlockState(blockPos).getShape(this.world, blockPos), facing) && random.nextFloat() < 0.05) {
                        this.world.setBlock(edge, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing), 2);
                        break;
                    }
                }
            }

            this.generateChests(this.overallsize / 2, chests, drops, random);
        }

        private void digOutHole(Set<BlockPos> holePositions, Set<BlockPos> edgePositions, Random random, NonNullList<ItemStack> drops, Biome biome) {
            for (BlockPos holePosition : holePositions) {
                if (this.processHolePositions(holePositions, holePosition, random, biome)) {
                    edgePositions.add(holePosition);
                } else {
                    BlockState blockState = this.world.getBlockState(holePosition);
                    drops.addAll(Block.getDrops(blockState, this.world, holePosition, this.world.getBlockEntity(holePosition)));

                    BlockPos mutPos = WorldUtils.getTopNonLeavesBlock(this.world, holePosition, state -> (state.getMaterial().blocksMotion() && !state.getMaterial().isLiquid()));
                    while (mutPos.getY() >= holePosition.getY() && mutPos.getY() > 0) {
                        this.world.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                        mutPos = mutPos.below();
                    }
                }
                this.world.setBlock(holePosition, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        private void generateChests(int totalChest, Set<BlockPos> chests, NonNullList<ItemStack> drops, Random random) {
            NonNullList<ItemStack> mergedDrops = this.mergeDrops(drops);

            List<BlockPos> chestList = Lists.newArrayList(chests);
            Collections.shuffle(chestList, random);

            for (int i = 0; i < totalChest; i++) {
                if (i >= chestList.size()) {
                    break;
                }

                this.world.setBlock(chestList.get(i), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, this.getChestRotateAngle(random, chestList.get(i))), 2);
                TileEntity tileEntity = this.world.getBlockEntity(chestList.get(i));
                if (tileEntity instanceof LockableLootTileEntity) {

                    LockableLootTileEntity chest = (LockableLootTileEntity) tileEntity;
                    chest.setLootTable(LootTableHandler.CHEST_DIGSITE, random.nextLong());
                    chest.unpackLootTable(null);

                    if (!mergedDrops.isEmpty()) {
                        IntStream.range(0, chest.getContainerSize())
                            .boxed()
                            .collect(CollectorUtils.shuffler(random))
                            .filter(slot -> chest.getItem(slot).isEmpty())
                            .limit(random.nextInt(3) + 2)
                            .forEach(slot -> {
                                ItemStack stack = mergedDrops.get(random.nextInt(mergedDrops.size()));
                                int split = random.nextInt(17) + 3;
                                chest.setItem(slot, stack.split(split));
                            });

                    }
                }
            }
        }

        private Direction getChestRotateAngle(Random random, BlockPos chest) {
            if (random.nextBoolean()) {
                BlockState blockState = this.world.getBlockState(chest.relative(Direction.NORTH));
                if (blockState.getBlock().isAir(blockState, this.world, chest)) {
                    return Direction.NORTH;
                } else {
                    return Direction.SOUTH;
                }
            } else {
                BlockState blockState = this.world.getBlockState(chest.relative(Direction.EAST));
                if (blockState.getBlock().isAir(blockState, this.world, chest)) {
                    return Direction.EAST;
                } else {
                    return Direction.WEST;
                }
            }
        }

        private NonNullList<ItemStack> mergeDrops(NonNullList<ItemStack> drops) {
            NonNullList<ItemStack> mergedDrops = NonNullList.create();
            for (ItemStack drop : drops) {
                List<ItemStack> additions = Lists.newArrayList();
                boolean found = false;
                for (ItemStack mergedDrop : mergedDrops) {
                    if (mergedDrop.equals(drop, false)) {
                        int mergeCount = mergedDrop.getCount() + drop.getCount();
                        if (mergeCount <= mergedDrop.getMaxStackSize()) {
                            mergedDrop.setCount(mergeCount);
                        } else {
                            mergedDrop.setCount(mergedDrop.getMaxStackSize());
                            ItemStack newDrop = drop.copy();
                            newDrop.setCount(mergeCount - mergedDrop.getMaxStackSize());
                            additions.add(newDrop);
                        }
                        found = true;
                        break;
                    }
                }
                mergedDrops.addAll(additions);
                if (!found) {
                    mergedDrops.add(drop.copy());
                }
            }
            return mergedDrops;
        }

        //Return true if the position is next to a non position
        private boolean processHolePositions(Set<BlockPos> holePositions, BlockPos holePosition, Random random, Biome biome) {
            boolean xFree = false;
            boolean yFree = false;
            boolean zFree = false;
            for (Direction value : Direction.values()) {
                BlockPos pos = holePosition.relative(value);
                if (!holePositions.contains(pos)) {
                    if (value.getAxis() == Direction.Axis.X) {
                        xFree = !xFree;
                    } else if (value.getAxis() == Direction.Axis.Y) {
                        yFree = !yFree;
                    } else if (value.getAxis() == Direction.Axis.Z) {
                        zFree = !zFree;
                    }
                }
                if (value == Direction.UP) {
                    continue;
                }
                if (!holePositions.contains(pos)) { //The outline of the hole.
                    this.setHoldState(this.world.getBlockState(pos), pos, random, biome);
                }
            }
            return yFree && (xFree || zFree);
        }

        private void setHoldState(BlockState currentState, BlockPos pos, Random random, Biome biome) {
            if (!currentState.getBlock().canBeReplaced(currentState, Fluids.EMPTY)) {
                if (random.nextFloat() < 0.1 - 0.05 * (this.circles.length - this.centralPosition.getY() + pos.getY())) {
                    this.world.setBlock(pos, BlockUtils.getBiomeDependantState(Blocks.STONE.defaultBlockState(), biome), 2);
                } else if (random.nextFloat() < 0.1) {
                    this.world.setBlock(pos, BlockUtils.getBiomeDependantState(Blocks.GRASS.defaultBlockState(), biome), 2);
                } else if (random.nextFloat() < 0.095) {
                    this.world.setBlock(pos, BlockUtils.getBiomeDependantState(Blocks.COARSE_DIRT.defaultBlockState(), biome), 2);
                } else {
                    this.world.setBlock(pos, BlockUtils.getBiomeDependantState(currentState, biome), 2);
                }
            } else if (currentState.getMaterial().isLiquid()) {
                if(pos.getY() - this.centralPosition.getY() == -1) { //Top circle layer
                    BlockPos mut = pos;
                    while(this.world.getBlockState(mut).getMaterial().isLiquid()) {
                        this.world.setBlock(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.defaultBlockState(), biome), 2);
                        mut = mut.above();
                    }
                } else {
                    this.world.setBlock(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.defaultBlockState(), biome), 2);
                }
            }
        }

        private void generateCircularHole(Circle circle, int layer, Set<BlockPos> holePositions, Set<BlockPos> fossilPositions, Random random) {
            double distX = circle.distx;
            double distZ = circle.distz;

            float xsize = circle.xsize;
            float zsize = circle.zsize;

            int startx = circle.startx + this.centralPosition.getX();
            int startz = circle.startz + this.centralPosition.getZ();

            int rad = circle.rad;

            this.iterateCircle(distX, distZ, xsize, zsize, rad, (x, z) -> holePositions.add(new BlockPos(startx + x, this.centralPosition.getY() + layer - this.totalLayers, startz + z)));
            if (layer == 0) {
                int totalBlocks = Math.max(rad / 2, 5);
                int x = startx;
                int z = startz;
                while (totalBlocks > 0) {
                    x += random.nextInt(3) - 1;
                    z += random.nextInt(3) - 1;

                    BlockPos pos = new BlockPos(x, this.centralPosition.getY() + layer - this.totalLayers - 1, z);

                    if (!fossilPositions.contains(pos)) {
                        fossilPositions.add(pos);
                        totalBlocks--;
                    }
                }
            } else if (layer == this.totalLayers - 1) {
                this.iterateCircle(distX, distZ, xsize, zsize, rad, (x, z) -> {
                    BlockPos blockPos = new BlockPos(startx + x, this.centralPosition.getY() + layer - this.totalLayers, startz + z);
                    int ytop = this.getTopSolid(blockPos);
                    for (; blockPos.getY() <= ytop; blockPos = blockPos.above()) {
                        this.world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                });
            }
        }

        private int getTopSolid(BlockPos pos) {
            Chunk chunk = this.world.getChunkAt(pos);
            BlockPos blockpos;
            BlockPos blockpos1;

            for (blockpos = new BlockPos(pos.getX(), chunk.getHighestSectionPosition() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
                blockpos1 = blockpos.below();
                BlockState state = chunk.getBlockState(blockpos1);

                if ((state.getMaterial().blocksMotion() || state.getMaterial().isLiquid()) && !state.is(BlockTags.LEAVES)) {
                    break;
                }
            }

            return blockpos.getY();
        }

        private void setFossils(Set<BlockPos> fossilPositions, Dinosaur dinosaur) {
            //TODO
//            for (BlockPos pos : fossilPositions) {
//                if (!this.world.getBlockState(pos).canBeReplaced(Fluids.EMPTY)) {
//                    this.world.setBlock(pos, FossilBlock.FossilType.guess(this.world.getBlockState(pos), dinosaur), 2);
//                }
//            }
        }

        public void iterateCircle(double distX, double distZ, float xSize, float zSize, int radius, BiConsumer<Integer, Integer> consumer) {
            int minx = MathHelper.floor(distX);
            int minz = MathHelper.floor(distZ);
            for (int x = -minx; x < Math.ceil(distX); x++) {
                for (int z = -minz; z < Math.ceil(distZ); z++) {
                    if (xSize * x * x + zSize * z * z < radius * radius) {
                        consumer.accept(x, z);
                    }
                }
            }
        }
    }
}
