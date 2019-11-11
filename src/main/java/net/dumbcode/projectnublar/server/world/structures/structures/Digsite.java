package net.dumbcode.projectnublar.server.world.structures.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.block.FossilBlock;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.LootTableHandler;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Digsite extends Structure {

    private final int size;
    private final int children;

    public Digsite(int weight, int size, int children) {
        super(weight);
        this.size = size;
        this.children = children;
    }

    @Override
    public StructureInstance createInstance(World world, BlockPos pos, Random random) {
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
            int numcircles = random.nextInt(2) + 5;
            Circle[] layercircs = circles[layer] = new Circle[numcircles];
            for (int circle = 0; circle < numcircles; circle++) {
                int startx = pos.getX();
                int startz = pos.getZ();
                if (circle > 0) {
                    startx += random.nextInt(determinedSize);
                    startz += random.nextInt(determinedSize);
                }
                double distx = Math.sqrt((determinedSize*determinedSize)/xsize);
                double distz = Math.sqrt((determinedSize*determinedSize)/zsize);

                minx = Math.min(minx, -MathHelper.floor(distx));
                minz = Math.min(minz, -MathHelper.floor(distz));

                maxx = Math.max(maxx, MathHelper.ceil(distx));
                maxz = Math.max(maxz, MathHelper.ceil(distz));
                layercircs[circle] = new Circle(startx, startz, determinedSize, xsize, zsize, distx, distz);
            }
        }

        return new Instance(world, pos, overallsize, circles, this.children, totalLayers, new Vec2f(maxx-minx, maxz-minz));
    }

    @AllArgsConstructor
    private class Circle {int startx, startz, rad; float xsize, zsize; double distx, distz;}

    public class Instance extends StructureInstance {

        private final int overallsize;
        private final Circle[][] circles;
        private final int totalLayers;

        public Instance(World world, BlockPos position, int overallsize, Circle[][] circles, int children, int totalLayers, Vec2f size) {
            super(world, position.add(size.x/2, 0, size.y/2), children, (int) size.x, (int) size.y);
            this.overallsize = overallsize;
            this.circles = circles;
            this.totalLayers = totalLayers;
        }

        @Override
        public void build(Random random, List<DataHandler> handlers) {
            Set<BlockPos> holePositions = Sets.newHashSet();
            Set<BlockPos> fossilPositions = Sets.newHashSet();

            for (int layer = 0; layer < this.circles.length; layer++) {
                for (Circle circle : this.circles[layer]) {
                    this.generateCircularHole(circle, layer, holePositions, fossilPositions, random);
                }
            }

            this.setFossils(fossilPositions);

            this.generateHoleDecor(holePositions, random);
        }

        private void generateHoleDecor(Set<BlockPos> holePositions, Random random) {
            Set<BlockPos> chests = Sets.newHashSet();
            NonNullList<ItemStack> drops = NonNullList.create();
            Biome biome = this.world.getBiome(this.position);

            for (BlockPos holePosition : holePositions) {
                if(this.processHolePositions(holePositions, holePosition, random, biome)) {
                    if(random.nextFloat() < 0.1 && this.world.getBlockState(holePosition.down()).isSideSolid(this.world, holePosition, EnumFacing.UP)) {
                        this.world.setBlockState(holePosition, BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome));
                        this.world.setBlockState(holePosition.up(), BlockUtils.getBiomeDependantState(Blocks.TORCH.getDefaultState(), biome));
                    } else {
                        chests.add(holePosition);
                    }
                } else {
                    if(random.nextFloat() < 0.125) {
                        IBlockState blockState = this.world.getBlockState(holePosition);
                        blockState.getBlock().getDrops(drops, this.world, holePosition, blockState, 0);
                    }
                    this.world.setBlockState(holePosition, Blocks.AIR.getDefaultState());
                }
            }

            this.generateChests(this.overallsize / 2, chests, drops, random);
        }

        private void generateChests(int totalChest, Set<BlockPos> chests, NonNullList<ItemStack> drops, Random random) {
            NonNullList<ItemStack> mergedDrops = this.mergeDrops(drops);

            List<BlockPos> chestList = Lists.newArrayList(chests);
            Collections.shuffle(chestList, random);

            for (int i = 0; i < totalChest; i++) {
                if(i >= chestList.size()) {
                    break;
                }

                this.world.setBlockState(chestList.get(i), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, this.getChestRotateAngle(random, chestList.get(i))));
                TileEntity tileEntity = this.world.getTileEntity(chestList.get(i));
                if(tileEntity instanceof TileEntityLockableLoot) {

                    TileEntityLockableLoot chest = (TileEntityLockableLoot) tileEntity;
                    chest.setLootTable(LootTableHandler.CHEST_DIGSITE, random.nextLong());
                    chest.fillWithLoot(null);

                    if(!mergedDrops.isEmpty()) {
                        IntStream.range(0, chest.getSizeInventory())
                            .boxed()
                            .collect(IOCollectors.shuffler(random))
                            .filter(slot -> chest.getStackInSlot(slot).isEmpty())
                            .limit(random.nextInt(5) + 3)
                            .forEach(slot -> {

                                ItemStack stack = mergedDrops.get(random.nextInt(mergedDrops.size()));
                                int split = random.nextInt(17) + 3;
                                if(stack.getCount() > split) {
                                    stack = stack.splitStack(split);
                                }
                                chest.setInventorySlotContents(slot, stack);

                            });

                    }
                }
            }
        }

        private EnumFacing getChestRotateAngle(Random random, BlockPos chest) {
            if(random.nextBoolean()) {
                IBlockState blockState = this.world.getBlockState(chest.offset(EnumFacing.NORTH));
                if(blockState.getBlock().isAir(blockState, this.world, chest)) {
                    return EnumFacing.NORTH;
                } else {
                    return EnumFacing.SOUTH;
                }
            } else {
                IBlockState blockState = this.world.getBlockState(chest.offset(EnumFacing.EAST));
                if(blockState.getBlock().isAir(blockState, this.world, chest)) {
                    return EnumFacing.EAST;
                } else {
                    return EnumFacing.WEST;
                }
            }
        }

        private NonNullList<ItemStack> mergeDrops(NonNullList<ItemStack> drops) {
            NonNullList<ItemStack> mergedDrops = NonNullList.create();
            for (ItemStack drop : drops) {
                List<ItemStack> additions = Lists.newArrayList();
                boolean found = false;
                for (ItemStack mergedDrop : mergedDrops) {
                    if(mergedDrop.isItemEqual(drop)) {
                        int mergeCount = mergedDrop.getCount() + drop.getCount();
                        if(mergeCount <= mergedDrop.getMaxStackSize()) {
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
                if(!found) {
                    mergedDrops.add(drop.copy());
                }
            }
            return mergedDrops;
        }

        private boolean processHolePositions(Set<BlockPos> holePositions, BlockPos holePosition, Random random, Biome biome) {
            boolean xFree = false;
            boolean yFree = false;
            boolean zFree = false;
            for (EnumFacing value : EnumFacing.values()) {
                BlockPos pos = holePosition.offset(value);
                if(!holePositions.contains(pos)) {
                    if(value.getAxis() == EnumFacing.Axis.X) {
                        xFree = !xFree;
                    } else if(value.getAxis() == EnumFacing.Axis.Y) {
                        yFree = !yFree;
                    } else if(value.getAxis() == EnumFacing.Axis.Z) {
                        zFree = !zFree;
                    }
                }
                if(value == EnumFacing.UP) {
                    continue;
                }
                if(!holePositions.contains(pos)) {
                    this.setHoldState(this.world.getBlockState(pos), pos, random, biome);
                }
            }
            return xFree && yFree && zFree;
        }

        private void setHoldState(IBlockState currentState, BlockPos pos, Random random, Biome biome) {
            if(!currentState.getBlock().isReplaceable(this.world, pos)) {
                if(random.nextFloat() < 0.1 - 0.05 * (this.circles.length - this.position.getY() + pos.getY())){
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.STONE.getDefaultState(), biome));
                } else if(random.nextFloat() < 0.1){
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.GRASS.getDefaultState(), biome));
                } else if(random.nextFloat() < 0.095) {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                } else {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(currentState, biome));
                }
            } else if(currentState.getMaterial().isLiquid()) {
                this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.getDefaultState(), biome));
            }
        }

        private void generateCircularHole(Circle circle, int layer, Set<BlockPos> holePositions, Set<BlockPos> fossilPositions, Random random) {
            double distX = circle.distx;
            double distZ = circle.distz;

            float xsize = circle.xsize;
            float zsize = circle.zsize;

            int startx = circle.startx;
            int startz = circle.startz;


            int rad = circle.rad;

            for (Pair<Integer, Integer> coord : this.circleIterator(distX, distZ, xsize, zsize, rad)) {
                holePositions.add(new BlockPos(startx+coord.getLeft(), this.position.getY()+layer-this.totalLayers, startz+coord.getRight()));
            }
            if(layer == 0) {
                int totalBlocks = Math.max(rad/2, 5);
                int x = startx;
                int z = startz;
                while (totalBlocks > 0) {
                    x += random.nextInt(3) - 1;
                    z += random.nextInt(3) - 1;

                    BlockPos pos = new BlockPos(x, this.position.getY()+layer-this.totalLayers-1, z);

                    if(!fossilPositions.contains(pos)) {
                        fossilPositions.add(pos);
                        totalBlocks--;
                    }
                }
            } else if(layer == this.totalLayers -1) {
                for (Pair<Integer, Integer> coord : this.circleIterator(distX, distZ, xsize, zsize, rad)) {
                    BlockPos blockPos = new BlockPos(startx + coord.getLeft(), this.position.getY() + layer - this.totalLayers, startz + coord.getRight());
                    int ytop = this.world.getTopSolidOrLiquidBlock(blockPos).getY();
                    for (;blockPos.getY() <= ytop; blockPos = blockPos.up()) {
                        this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        private void setFossils(Set<BlockPos> fossilPositions) {
            for (BlockPos pos : fossilPositions) {
                if(!this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
                    this.world.setBlockState(pos, FossilBlock.FossilType.guess(this.world.getBlockState(pos), DinosaurHandler.TYRANNOSAURUS));
                }
            }
        }

        public Iterable<Pair<Integer, Integer>> circleIterator(double distX, double distZ, float xSize, float zSize, int radius) {
            int minX = -MathHelper.floor(distX);
            int maxX = MathHelper.ceil(distX);
            int rangeX = maxX - minX;

            int minZ = -MathHelper.floor(distZ);
            int maxZ = MathHelper.ceil(distZ);
            int rangeZ = maxZ - minZ;

            return () -> new Iterator<Pair<Integer, Integer>>() {

                private int currentValue;

                private int cachedValue = -1;

                @Override
                public boolean hasNext() {
                    return this.searchNextValue() == -1;
                }

                @Override
                public Pair<Integer, Integer> next() {
                    int value = this.cachedValue == -1 ? this.searchNextValue() : this.cachedValue;
                    if(value == -1) {
                        throw new NoSuchElementException("Iterator Reached End of Circle");
                    }
                    this.cachedValue = -1;
                    return Pair.of(value % rangeX, value / rangeZ);
                }

                private int searchNextValue() {
                    int x;
                    int z;

                    do {
                        x = this.currentValue % rangeX;
                        z = this.currentValue / rangeZ;

                        if(this.currentValue > rangeX*rangeZ) {
                            return -1;
                        }

                        this.currentValue++;
                    } while (xSize*x*x + zSize*z*z >= radius*radius);

                    this.cachedValue = -1;
                    return currentValue;
                }
            };
        }

        @Override
        public boolean canBuild() { //todo: more predicates, and make the constants not so constant
            double[] data = new double[(this.xSize + 1) * (this.zSize + 1)];
            AtomicInteger pointer = new AtomicInteger();

            AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);
            AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);

            AtomicInteger liquids = new AtomicInteger();
            AtomicInteger solids = new AtomicInteger();


            this.traverseTopdown(blockpos -> {
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
