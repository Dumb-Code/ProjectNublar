package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.utils.PylonNetworkSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockPylonHead extends Block implements IItemBlock {

    private static final VoxelShape BASE_POLE_SHAPE = box(6, 0, 6, 10, 10, 10);
    private static final VoxelShape POLE_RING_1 = box(5.5, 3, 5.5, 10.5, 4, 10.5);
    private static final VoxelShape POLE_RING_2 = box(5.5, 4.5, 5.5, 10.5, 5.5, 10.5);
    private static final VoxelShape POLE_RING_3 = box(5.5, 6, 5.5, 10.5, 7, 10.5);
    private static final VoxelShape POLE_RING_4 = box(5.5, 7.5, 5.5, 10.5, 8.5, 10.5);
    private static final VoxelShape TOP_POLE = box(7.75, 10, 7.75, 8.25, 14.4, 8.25);
    private static final VoxelShape TOP_RING_1 = box(6.8, 10.3, 6.8, 9.2, 10.7, 9.2);
    private static final VoxelShape TOP_RING_2 = box(6.8, 11.3, 6.8, 9.2, 11.7, 9.2);
    private static final VoxelShape TOP_RING_3 = box(6.8, 11.8, 6.8, 9.2, 12.3, 9.2);
    private static final VoxelShape TOP_RING_4 = box(6.8, 12.9, 6.8, 9.2, 13.3, 9.2);

    private static final VoxelShape TOP_RING_BAR_SEGMENT = box(3.7, 11.8, 6.2, 4.3, 12.4, 9.8);
    private static final VoxelShape TOP_RING_INNER_SEGMENT = box(3.83, 12.17, 7.83, 6.8, 11.83, 8.17);
    private static final VoxelShape TOP_RING_DIAGONAL_BAR = VoxelShapes.or(
        box(4.25, 11.8, 9.75, 5.275, 12.4, 11.065),
        box(4.7875, 11.8, 10.4325, 5.7625, 12.4, 11.6975),
        box(5.275, 11.8, 11.065, 6.3, 12.4, 12.38)
    );
    private static final VoxelShape TOP_RING = VoxelShapes.or(TOP_RING_BAR_SEGMENT, TOP_RING_INNER_SEGMENT, TOP_RING_DIAGONAL_BAR);

    private static final VoxelShape TOP_RING_ROT1 = rotateShape(1, TOP_RING);
    private static final VoxelShape TOP_RING_ROT2 = rotateShape(2, TOP_RING);
    private static final VoxelShape TOP_RING_ROT3 = rotateShape(3, TOP_RING);

    private static final VoxelShape SHAPE = VoxelShapes.or(
        BASE_POLE_SHAPE, POLE_RING_1, POLE_RING_2, POLE_RING_3, POLE_RING_4, TOP_POLE,
        TOP_RING_1, TOP_RING_2, TOP_RING_3, TOP_RING_4,
        TOP_RING, TOP_RING_ROT1, TOP_RING_ROT2, TOP_RING_ROT3
    );

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockPylonHead(Properties p_i48440_1_) {
        super(p_i48440_1_);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult ray) {
        ItemStack item = playerIn.getItemInHand(hand);
        if(item.getItem() == ItemHandler.WIRE_SPOOL.get() && !worldIn.isClientSide) {
            CompoundNBT nbt = item.getOrCreateTagElement(ProjectNublar.MODID);
            if(nbt.contains("pylon_position", Constants.NBT.TAG_COMPOUND)) {
                BlockPos other = NBTUtil.readBlockPos(nbt.getCompound("pylon_position"));
                if(!pos.equals(other)) {
                    PylonHeadBlockEntity.Connection connection = new PylonHeadBlockEntity.Connection(other, pos);

                    TileEntity entity = worldIn.getBlockEntity(pos);
                    TileEntity otherEntity = worldIn.getBlockEntity(other);
                    if(entity instanceof PylonHeadBlockEntity && otherEntity instanceof PylonHeadBlockEntity) {
                        PylonHeadBlockEntity thisPylon = (PylonHeadBlockEntity) entity;
                        PylonHeadBlockEntity otherPylon = (PylonHeadBlockEntity) otherEntity;

                        UUID networkUUID = thisPylon.getNetworkUUID();
                        UUID oldNetworkUUID = otherPylon.getNetworkUUID();
                        if(!networkUUID.equals(oldNetworkUUID)) { //If the same an inter-network-connection is formed and we shouldn't mess with adding/removing
                            PylonNetworkSavedData data = PylonNetworkSavedData.getData((ServerWorld) worldIn);

                            Set<BlockPos> newSet = data.getPositions(thisPylon);
                            Set<BlockPos> otherSet = data.getPositions(otherPylon);

                            for (BlockPos blockPos : otherSet) {
                                TileEntity te = worldIn.getBlockEntity(blockPos);
                                if(te instanceof PylonHeadBlockEntity) {
                                    ((PylonHeadBlockEntity) te).setNetworkUUID(networkUUID);
                                    ((PylonHeadBlockEntity) te).syncToClient();
                                    newSet.add(blockPos);
                                    te.setChanged();
                                }
                            }
                            data.cleanAndClear(oldNetworkUUID);
                            data.setDirty();

                        }
                        thisPylon.addConnection(connection);
                        otherPylon.addConnection(connection);
                    }

                }
                nbt.remove("pylon_position");
            } else {
                nbt.put("pylon_position", NBTUtil.writeBlockPos(pos));
            }
            return ActionResultType.SUCCESS;
        }
        return super.use(state, worldIn, pos, playerIn, hand, ray);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean flag) {
        TileEntity old = worldIn.getBlockEntity(pos);
        super.onRemove(state, worldIn, pos, newState, flag);
        if(worldIn instanceof ServerWorld && old instanceof PylonHeadBlockEntity) {
            ServerWorld world = (ServerWorld) worldIn;
            //Remove connections from other pylons
            PylonHeadBlockEntity pylon = (PylonHeadBlockEntity) old;
            for (PylonHeadBlockEntity.Connection connection : pylon.getConnections()) {
                TileEntity otherEntity = world.getBlockEntity(connection.getOther(pos));
                if(otherEntity instanceof PylonHeadBlockEntity) {
                    ((PylonHeadBlockEntity) otherEntity).getConnections().remove(connection);
                    ((PylonHeadBlockEntity) otherEntity).syncToClient();
                    otherEntity.setChanged();
                }
            }

            //Remove this pylon from the pylon network
            PylonNetworkSavedData data = PylonNetworkSavedData.getData(world);
            data.cleanAndClear(pylon.getNetworkUUID());


            //Make sure that pylons that are not split are have different pylon network IDs
            Set<Set<BlockPos>> branchedPositions = new HashSet<>();
            for (PylonHeadBlockEntity.Connection connection : pylon.getConnections()) {
                TileEntity otherEntity = world.getBlockEntity(connection.getOther(pos));
                if(otherEntity instanceof PylonHeadBlockEntity) {
                    branchedPositions.add(((PylonHeadBlockEntity) otherEntity).gatherAllNetworkLocations(new HashSet<>()));
                }
            }

            for (Set<BlockPos> set : branchedPositions) {
                UUID newUUID = UUID.randomUUID();
                Set<BlockPos> positions = data.getPositions(newUUID);
                for (BlockPos blockPos : set) {
                    TileEntity entity = world.getBlockEntity(blockPos);
                    if(entity instanceof PylonHeadBlockEntity) {
                        ((PylonHeadBlockEntity) entity).setNetworkUUID(newUUID);
                        entity.setChanged();
                        ((PylonHeadBlockEntity) entity).syncToClient();
                        positions.add(blockPos);
                    }
                }
            }
        }
    }


    @Override
    @OnlyIn(Dist.CLIENT) //This is here as we don't want this method on dedicated servers.
    public void stepOn(World worldIn, BlockPos pos, Entity entityIn) {
        if(ProjectNublar.DEBUG) {
            TileEntity tileEntity = worldIn.getBlockEntity(pos);
            if(tileEntity instanceof PylonHeadBlockEntity && !worldIn.isClientSide) {
                Minecraft.getInstance().player.displayClientMessage(new StringTextComponent(((PylonHeadBlockEntity) tileEntity).getNetworkUUID().toString()), false);
            }
        }
        super.stepOn(worldIn, pos, entityIn);
    }


    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean drops) {
        BlockState s = worldIn.getBlockState(pos.below());
        if(s.getBlock() != BlockHandler.PYLON_POLE.get() && !Block.isFaceFull(s.getCollisionShape(worldIn, pos.below()), Direction.UP)) {
            worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, drops);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PylonHeadBlockEntity();
    }

    public static VoxelShape rotateShape(int times, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1], VoxelShapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }
}
