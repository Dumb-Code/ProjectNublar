package net.dumbcode.projectnublar.server.world.structures.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.FossilBlock;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

        int maxx = Integer.MIN_VALUE;
        int maxz = Integer.MIN_VALUE;

        for (int layer = 0; layer < totalLayers; layer++) {
            float xsize = random.nextFloat() + 0.5F; //Between 0.5 and 1.5;
            float zsize = random.nextFloat() + 0.5F;;
            int size = ((layer) + overallsize + 1);
            int numcircles = random.nextInt(2) + 5;
            Circle[] layercircs = circles[layer] = new Circle[numcircles];
            for (int circle = 0; circle < numcircles; circle++) {
                int startx = pos.getX();
                int startz = pos.getZ();
                if (circle > 0) {
                    startx += random.nextInt(size);
                    startz += random.nextInt(size);
                }
                double distx = Math.sqrt((size*size)/xsize);
                double distz = Math.sqrt((size*size)/zsize);

                minx = Math.min(minx, -MathHelper.floor(distx));
                minz = Math.min(minz, -MathHelper.floor(distz));

                maxx = Math.max(maxx, MathHelper.ceil(distx));
                maxz = Math.max(maxz, MathHelper.ceil(distz));
                layercircs[circle] = new Circle(startx, startz, size, xsize, zsize, distx, distz);
            }
        }

        return new Instance(world, pos, overallsize, circles, this.children, totalLayers, maxx-minx, maxz-minz);
    }

    @AllArgsConstructor
    private class Circle {int startx, startz, rad; float xsize, zsize; double distx, distz;}

    public class Instance extends StructureInstance {

        private final int overallsize;
        private final Circle[][] circles;
        private final int totalLayers;

        public Instance(World world, BlockPos position, int overallsize, Circle[][] circles, int children, int totalLayers, int xSize, int zSize) {
            super(world, position.add(xSize/2, 0, zSize/2), children, xSize, zSize);
            this.overallsize = overallsize;
            this.circles = circles;
            this.totalLayers = totalLayers;
        }

        @Override
        public void build(Random random, List<DataHandler> handlers) {
            Set<BlockPos> holePositions = Sets.newHashSet();
            Set<BlockPos> fossilPositions = Sets.newHashSet();

            for (int layer = 0; layer < this.circles.length; layer++) {
                Circle[] circles = this.circles[layer];
                for (int c = 0; c < circles.length; c++) {

                    double distX = circles[c].distx;
                    double distZ = circles[c].distz;

                    float xsize = circles[c].xsize;
                    float zsize = circles[c].zsize;

                    int startx = circles[c].startx;
                    int startz = circles[c].startz;


                    int rad = circles[c].rad;

                    for (int x = -MathHelper.floor(distX); x < Math.ceil(distX); x++) {
                        for (int z = -MathHelper.floor(distZ); z < Math.ceil(distZ); z++) {
                            if(xsize * x * x + zsize * z * z < rad*rad) {
                                holePositions.add(new BlockPos(startx+x, this.position.getY()+layer-this.totalLayers, startz+z));
                            }
                        }
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
                        for (int x = -MathHelper.floor(distX); x < Math.ceil(distX); x++) {
                            for (int z = -MathHelper.floor(distZ); z < Math.ceil(distZ); z++) {
                                if(xsize * x * x + zsize * z * z < rad*rad) {
                                    BlockPos blockPos = new BlockPos(startx + x, this.position.getY() + layer - this.totalLayers, startz + z);
                                    int ytop = this.world.getTopSolidOrLiquidBlock(blockPos).getY();
                                    for (;blockPos.getY() <= ytop; blockPos = blockPos.up()) {
                                        this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (BlockPos pos : fossilPositions) {
                if(!this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
                    this.world.setBlockState(pos, FossilBlock.FossilType.guess(this.world.getBlockState(pos), DinosaurHandler.TYRANNOSAURUS));
                }
            }

            Biome biome = this.world.getBiome(this.position);

            Set<BlockPos> torches = Sets.newHashSet();

            Set<BlockPos> chests = Sets.newHashSet();

            NonNullList<ItemStack> drops = NonNullList.create();

            for (BlockPos holePosition : holePositions) {
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
                    IBlockState state = this.world.getBlockState(pos);
                    if(!holePositions.contains(pos)) {
                        if(!state.getBlock().isReplaceable(this.world, pos)) {
                            if(random.nextFloat() < 0.1 - 0.05 * (this.circles.length - this.position.getY() + pos.getY())){
                                this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.STONE.getDefaultState(), biome));
                            } else if(random.nextFloat() < 0.1){
                                this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.GRASS.getDefaultState(), biome));
                            } else if(random.nextFloat() < 0.1) {
                                this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                            } else {
                                this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(state, biome));
                            }
                        } else if(state.getMaterial().isLiquid()) {
                            this.world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.getDefaultState(), biome));
                        }
                    }
                }

                if(xFree && yFree && zFree) {
                    if(random.nextFloat() < 0.1 && this.world.getBlockState(holePosition.down()).isSideSolid(this.world, holePosition, EnumFacing.UP)) {
                        this.world.setBlockState(holePosition, BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome));
                        torches.add(holePosition.up());
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

            int totalChest = this.overallsize / 2;

            List<BlockPos> chestList = Lists.newArrayList(chests);
            Collections.shuffle(chestList, random);

            for (int i = 0; i < totalChest; i++) {
                if(i >= chestList.size()) {
                    break;
                }
                EnumFacing rotate;
                if(random.nextBoolean()) {
                    IBlockState blockState = this.world.getBlockState(chestList.get(i).offset(EnumFacing.NORTH));
                    if(blockState.getBlock().isAir(blockState, this.world, chestList.get(i))) {
                        rotate = EnumFacing.NORTH;
                    } else {
                        rotate = EnumFacing.SOUTH;
                    }
                } else {
                    IBlockState blockState = this.world.getBlockState(chestList.get(i).offset(EnumFacing.EAST));
                    if(blockState.getBlock().isAir(blockState, this.world, chestList.get(i))) {
                        rotate = EnumFacing.EAST;
                    } else {
                        rotate = EnumFacing.WEST;
                    }
                }
                this.world.setBlockState(chestList.get(i), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, rotate));
                TileEntity tileEntity = this.world.getTileEntity(chestList.get(i));
                if(tileEntity instanceof TileEntityLockableLoot) {
                    TileEntityLockableLoot chest = (TileEntityLockableLoot) tileEntity;
                    chest.setLootTable(LootTableHandler.CHEST_DIGSITE, random.nextLong());
                    chest.fillWithLoot(null);
                    int inventory = chest.getSizeInventory();
                    List<Integer> scrambledList = Lists.newArrayList();
                    for (int slot = 0; slot < inventory; slot++) {
                        scrambledList.add(slot);
                    }
                    Collections.shuffle(scrambledList, random);
                    int totalslots = random.nextInt(5) + 3;
                    for (int index = 0; index < totalslots; index++) {
                        int slot = scrambledList.get(index);
                        if(mergedDrops.isEmpty()) {
                            break;
                        }
                        if(chest.getStackInSlot(slot).isEmpty()) {
                            ItemStack stack = mergedDrops.get(random.nextInt(mergedDrops.size()));
                            int split = random.nextInt(17) + 3;
                            if(stack.getCount() > split) {
                                stack = stack.splitStack(split);
                            }
                            chest.setInventorySlotContents(slot, stack);
                        }
                     }
                }
            }

            for (BlockPos torch : torches) {
                if(this.world.getBlockState(torch.down()) == BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome)) {
                    this.world.setBlockState(torch, BlockUtils.getBiomeDependantState(Blocks.TORCH.getDefaultState(), biome));
                }
            }
        }

        @Override
        public boolean canBuild() { //todo: more predicates, and make the constants not so constant
            double[] data = new double[this.xSize * this.zSize + this.xSize + this.zSize + 1];
            int pointer = 0;

            float liquids = 0;
            float solids = 0;

            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;


            for (int x = -this.xSize/2; x < this.xSize/2; x++) {
                for (int z = -this.zSize/2; z < this.zSize/2; z++) {
                    BlockPos pos = this.position.add(x, 0, z);

                    Chunk chunk = this.world.getChunk(pos);
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
            return MathUtils.meanDeviation(data) <= 2;
        }
    }
}
