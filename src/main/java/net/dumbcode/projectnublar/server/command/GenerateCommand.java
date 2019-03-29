package net.dumbcode.projectnublar.server.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.utils.BlockUtils;
import net.dumbcode.projectnublar.server.utils.BuilderNode;
import net.dumbcode.projectnublar.server.world.structures.Structure;
import net.dumbcode.projectnublar.server.world.structures.network.NetworkBuilder;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.*;

public class GenerateCommand extends CommandBase {
    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "todo";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        Random random = new Random();
        World world = sender.getEntityWorld();
        int y = sender.getPosition().getY();


        int overallsize = (int) (Math.abs(random.nextGaussian()) * 2) + 1;

        int totalLayers = 2 + random.nextInt(2);


        float[][] layersizes = new float[totalLayers][2];
        for (int layer = 0; layer < totalLayers; layer++) {
            layersizes[layer][0] = random.nextFloat() * 2F + 0.5F; //Between 0.5 and 2.5
            layersizes[layer][1] = random.nextFloat() * 2F + 0.5F;
        }

        Set<BlockPos> holePositions = Sets.newHashSet();
        Set<BlockPos> fossilPositions = Sets.newHashSet();

        for (int layer = 0; layer < totalLayers; layer++) {
            float xsize = layersizes[layer][0];
            float zsize = layersizes[layer][1];
            int size = (int) ((layer*1.5F) + overallsize + 2);
            for (int sphere = 0; sphere < random.nextInt(2) + 5; sphere++) {
                int startx = sender.getPosition().getX();
                int startz = sender.getPosition().getZ();
                if(sphere > 0) {
                    startx += random.nextInt(size);
                    startz += random.nextInt(size);
                }


                for (int x = -size; x < size; x++) {
                    for (int z = -size; z < size; z++) {
                        float dist = xsize * x * x + zsize * z * z;
                        if(dist < size*size) {
                            holePositions.add(new BlockPos(startx+x, y+layer-totalLayers, startz+z));
                        }
                    }
                }
                if(layer == 0) {
                    int totalBlocks = Math.max(size/2, 5);
                    int x = startx;
                    int z = startz;
                    while (totalBlocks > 0) {
                        x += random.nextInt(3) - 1;
                        z += random.nextInt(3) - 1;

                        BlockPos pos = new BlockPos(x, y+layer-totalLayers-1, z);

                        if(!fossilPositions.contains(pos)) {
                            fossilPositions.add(pos);
                            totalBlocks--;
                        }
                    }
                } else if(layer == totalLayers -1) {
                    for (int x = -size; x < size; x++) {
                        for (int z = -size; z < size; z++) {
                            if(xsize * x * x + zsize * z * z < size*size) {
                                BlockPos blockPos = new BlockPos(startx + x, y + layer - totalLayers, startz + z);
                                int ytop = world.getTopSolidOrLiquidBlock(blockPos).getY();
                                for (;blockPos.getY() <= ytop; blockPos = blockPos.up()) {
                                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (BlockPos pos : fossilPositions) {
            if(random.nextFloat() <  0.25) {
                world.setBlockState(pos, Blocks.COAL_BLOCK.getDefaultState());
            } else {
                world.setBlockState(pos, BlockHandler.FOSSIlS.get(DinosaurHandler.TYRANNOSAURUS).getDefaultState());
            }
        }

        Biome biome = world.getBiome(sender.getPosition());

        Set<BlockPos> torches = Sets.newHashSet();

        Set<BlockPos> chests = Sets.newHashSet();

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
                IBlockState state = world.getBlockState(pos);
                if(!holePositions.contains(pos)) {
                    if(!state.getBlock().isReplaceable(world, pos)) {
                        if(random.nextFloat() < 0.1 - 0.05 * (totalLayers - y + pos.getY())){
                            world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.STONE.getDefaultState(), biome));
                        } else if(random.nextFloat() < 0.1){
                            world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.GRASS.getDefaultState(), biome));
                        } else if(random.nextFloat() < 0.1) {
                            world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), biome));
                        } else {
                            world.setBlockState(pos, BlockUtils.getBiomeDependantState(state, biome));
                        }
                    } else if(state.getMaterial().isLiquid()) {
                        world.setBlockState(pos, BlockUtils.getBiomeDependantState(Blocks.COBBLESTONE.getDefaultState(), biome));
                    }
                }
            }

            if(xFree && yFree && zFree) {
                if(random.nextFloat() < 0.1 && world.getBlockState(holePosition.down()).isSideSolid(world, holePosition, EnumFacing.UP)) {
                    world.setBlockState(holePosition, BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome));
                    torches.add(holePosition.up());
                } else {
                    chests.add(holePosition);
                }
            } else {
                world.setBlockState(holePosition, Blocks.AIR.getDefaultState());
            }
        }

        int totalChest = overallsize / 2;

        List<BlockPos> chestList = Lists.newArrayList(chests);
        Collections.shuffle(chestList, random);

        for (int i = 0; i < totalChest; i++) {
            if(i >= chestList.size()) {
                break;
            }
            EnumFacing rotate;
            if(random.nextBoolean()) {
                IBlockState blockState = world.getBlockState(chestList.get(i).offset(EnumFacing.NORTH));
                if(blockState.getBlock().isAir(blockState, world, chestList.get(i))) {
                    rotate = EnumFacing.NORTH;
                } else {
                    rotate = EnumFacing.SOUTH;
                }
            } else {
                IBlockState blockState = world.getBlockState(chestList.get(i).offset(EnumFacing.EAST));
                if(blockState.getBlock().isAir(blockState, world, chestList.get(i))) {
                    rotate = EnumFacing.EAST;
                } else {
                    rotate = EnumFacing.WEST;
                }
            }
            world.setBlockState(chestList.get(i), Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, rotate));
            TileEntity tileEntity = world.getTileEntity(chestList.get(i));
            if(tileEntity instanceof TileEntityLockableLoot) {
                ((TileEntityLockableLoot) tileEntity).setLootTable(LootTableList.CHESTS_DESERT_PYRAMID, random.nextLong());
            }
        }

        for (BlockPos torch : torches) {
            if(world.getBlockState(torch.down()) == BlockUtils.getBiomeDependantState(Blocks.OAK_FENCE.getDefaultState(), biome)) {
                world.setBlockState(torch, BlockUtils.getBiomeDependantState(Blocks.TORCH.getDefaultState(), biome));
            }
        }





//        new NetworkBuilder(sender.getEntityWorld(), sender.getPosition())
//                .generate(new Random(),
//                        BuilderNode.builder(Structure.class)
//                                        .child(new Structure("test", 5, 3, 1, 12))
//                                            .child(new Structure("test", 5, 6, 12, 1))
//                                            .sibling(new Structure("test", 2, 1, 6, 1))
//                                            .end()
//                                        .sibling(new Structure("test", 5, 3, 1, 12))
//                                            .child(new Structure("test", 2, 6, 12, 1))
//                                            .sibling(new Structure("test", 4, 2, 6, 1))
//                                            .sibling(new Structure("test", 1, 5, 6, 1))
//                                            .sibling(new Structure("test", 2, 3, 6, 1))
//                                            .end()
//                                        .sibling(new Structure("test", 5, 3, 20, 4))
//                                            .child(new Structure("test", 2, 6, 12, 1))
//                                            .sibling(new Structure("test", 4, 2, 12, 1))
//                                                .child(new Structure("", 12, 22, 15, 2))
//                                                .end()
//                                            .sibling(new Structure("test", 2, 3, 6, 1))
//                                            .end()
//                                        .sibling(new Structure("test", 5, 3, 1, 3))
//                                            .child(new Structure("test", 2, 6, 12, 1))
//                                            .sibling(new Structure("test", 2, 5, 6, 1))
//                                            .end()
//                                .buildToRoots());
    }
}
