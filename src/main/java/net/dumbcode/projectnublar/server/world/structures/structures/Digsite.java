package net.dumbcode.projectnublar.server.world.structures.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.block.FossilBlock;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.world.LootTableHandler;
import net.dumbcode.projectnublar.server.world.constants.StructureConstants;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.util.vector.Vector4f;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Digsite extends Structure {

    private final int size;

    public Digsite(int weight, int size, int children) {
        super(weight, children);
        this.size = size;
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

        return new Instance(world, pos.add(minx, 0, minz), overallsize, circles, totalLayers, new Vector4f(minx, maxx, minz, maxz), this);
    }

    @AllArgsConstructor
    private class Circle {int startx, startz, rad; float xsize, zsize; double distx, distz;}

    public class Instance extends StructureInstance {

        private final int overallsize;
        private final Circle[][] circles;
        private final int totalLayers;
        private final BlockPos centralPosition;

        public Instance(World world, BlockPos position, int overallsize, Circle[][] circles, int totalLayers, Vector4f dims, Digsite structure) {
            super(world, position, (int) (dims.y - dims.x), (int) (dims.w - dims.x), structure);
            this.overallsize = overallsize;
            this.circles = circles;
            this.totalLayers = totalLayers;
            this.centralPosition =  BlockUtils.getTopSolid(world, new BlockPos(position.getX() - dims.x, 257, position.getZ() - dims.z));
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

            this.setFossils(fossilPositions);

            this.generateHoleDecor(holePositions, random);
        }

        private void generateHoleDecor(Set<BlockPos> holePositions, Random random) {
            Set<BlockPos> chests = Sets.newHashSet();
            Set<BlockPos> edgePositions = Sets.newHashSet();

            NonNullList<ItemStack> drops = NonNullList.create();
            Biome biome = this.world.getBiome(this.centralPosition);

            this.digOutHole(holePositions, edgePositions, random, drops, biome);

            for (BlockPos edge : edgePositions) {
                if(this.world.getBlockState(edge.down()).isSideSolid(this.world, edge.down(), EnumFacing.UP)) {
                    if (random.nextFloat() < 0.95) {
                        chests.add(edge);
                    } else {
                        this.world.setBlockState(edge, BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome));
                        this.world.setBlockState(edge.up(), BlockUtils.getBiomeDependantState(Blocks.TORCH.getDefaultState(), biome));
                    }
                }
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    BlockPos blockPos = edge.offset(facing.getOpposite());
                    if(this.world.getBlockState(blockPos).isSideSolid(this.world, blockPos, facing) && random.nextFloat() < 0.05) {
                        this.world.setBlockState(edge, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, facing));
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
                    IBlockState blockState = this.world.getBlockState(holePosition);
                    blockState.getBlock().getDrops(drops, this.world, holePosition, blockState, 0);

                    BlockPos mutPos = WorldUtils.getTopNonLeavesBlock(this.world, holePosition, state -> (state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid()));
                    while (mutPos.getY() >= holePosition.getY() && mutPos.getY() > 0) {
                        this.world.setBlockState(mutPos, Blocks.AIR.getDefaultState());
                        mutPos = mutPos.down();
                    }
                }
                this.world.setBlockState(holePosition, Blocks.AIR.getDefaultState());
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

                this.world.setBlockState(chestList.get(i), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, this.getChestRotateAngle(random, chestList.get(i))));
                TileEntity tileEntity = this.world.getTileEntity(chestList.get(i));
                if (tileEntity instanceof TileEntityLockableLoot) {

                    TileEntityLockableLoot chest = (TileEntityLockableLoot) tileEntity;
                    chest.setLootTable(LootTableHandler.CHEST_DIGSITE, random.nextLong());
                    chest.fillWithLoot(null);

                    if (!mergedDrops.isEmpty()) {
                        IntStream.range(0, chest.getSizeInventory())
                            .boxed()
                            .collect(IOCollectors.shuffler(random))
                            .filter(slot -> chest.getStackInSlot(slot).isEmpty())
                            .limit(random.nextInt(3) + 2)
                            .forEach(slot -> {
                                ItemStack stack = mergedDrops.get(random.nextInt(mergedDrops.size()));
                                int split = random.nextInt(17) + 3;
                                chest.setInventorySlotContents(slot, stack.splitStack(split));

                            });

                    }
                }
            }
        }

        private EnumFacing getChestRotateAngle(Random random, BlockPos chest) {
            if (random.nextBoolean()) {
                IBlockState blockState = this.world.getBlockState(chest.offset(EnumFacing.NORTH));
                if (blockState.getBlock().isAir(blockState, this.world, chest)) {
                    return EnumFacing.NORTH;
                } else {
                    return EnumFacing.SOUTH;
                }
            } else {
                IBlockState blockState = this.world.getBlockState(chest.offset(EnumFacing.EAST));
                if (blockState.getBlock().isAir(blockState, this.world, chest)) {
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
                    if (mergedDrop.isItemEqual(drop)) {
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
            for (EnumFacing value : EnumFacing.values()) {
                BlockPos pos = holePosition.offset(value);
                if (!holePositions.contains(pos)) {
                    if (value.getAxis() == EnumFacing.Axis.X) {
                        xFree = !xFree;
                    } else if (value.getAxis() == EnumFacing.Axis.Y) {
                        yFree = !yFree;
                    } else if (value.getAxis() == EnumFacing.Axis.Z) {
                        zFree = !zFree;
                    }
                }
                if (value == EnumFacing.UP) {
                    continue;
                }
                if (!holePositions.contains(pos)) { //The outline of the hole.
                    this.setHoldState(this.world.getBlockState(pos), pos, random, biome);
                }
            }
            return yFree && (xFree || zFree);
        }

        private void setHoldState(IBlockState currentState, BlockPos pos, Random random, Biome biome) {
            if (!currentState.getBlock().isReplaceable(this.world, pos)) {
                if (random.nextFloat() < 0.1 - 0.05 * (this.circles.length - this.centralPosition.getY() + pos.getY())) {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.STONE.getDefaultState(), biome));
                } else if (random.nextFloat() < 0.1) {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.GRASS.getDefaultState(), biome));
                } else if (random.nextFloat() < 0.095) {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                } else {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(currentState, biome));
                }
            } else if (currentState.getMaterial().isLiquid()) {
                if(pos.getY() - this.centralPosition.getY() == -1) { //Top circle layer
                    BlockPos mut = pos;
                    while(this.world.getBlockState(mut).getMaterial().isLiquid()) {
                        this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.getDefaultState(), biome));
                        mut = mut.up();
                    }
                } else {
                    this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.getDefaultState(), biome));
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
                    for (; blockPos.getY() <= ytop; blockPos = blockPos.up()) {
                        this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                    }
                });
            }
        }

        private int getTopSolid(BlockPos pos) {
            Chunk chunk = this.world.getChunk(pos);
            BlockPos blockpos;
            BlockPos blockpos1;

            for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
                blockpos1 = blockpos.down();
                IBlockState state = chunk.getBlockState(blockpos1);

                if ((state.getMaterial().blocksMovement() || state.getMaterial().isLiquid()) && !state.getBlock().isLeaves(state, this.world, blockpos1) && !state.getBlock().isFoliage(this.world, blockpos1)) {
                    break;
                }
            }

            return blockpos.getY();
        }

        private void setFossils(Set<BlockPos> fossilPositions) {
            for (BlockPos pos : fossilPositions) {
                if (!this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
                    this.world.setBlockState(pos, FossilBlock.FossilType.guess(this.world.getBlockState(pos), DinosaurHandler.TYRANNOSAURUS));
                }
            }
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
