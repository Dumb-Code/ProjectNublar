package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.ConnectionType;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Mth;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class BlockElectricFencePole extends BlockConnectableBase implements IItemBlock {
    @Getter
    private final ConnectionType type;
    public final IntegerProperty indexProperty;
    public static final BooleanProperty POWERED_PROPERTY = BooleanProperty.create("powered");
    private final StateContainer<Block, BlockState> stateDefinition;

    private static boolean destroying = false;

    public static final int LIMIT = 15;

    public BlockElectricFencePole(Properties properties, ConnectionType type) {
        super(properties);
        this.type = type;
        this.indexProperty = IntegerProperty.create("index", 0, type.getHeight() - 1);

        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.createBlockStateDefinition(builder);
        builder.add(indexProperty);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);

        this.registerDefaultState(this.stateDefinition.any().setValue(indexProperty, 0).setValue(POWERED_PROPERTY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED_PROPERTY);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public StateContainer<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    //    @Override
    //    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos) {
    //
    //        return super.getInteractionShape(state, world, pos);
    //    }
    @Override
    protected VoxelShape getDefaultShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity entity = world.getBlockEntity(pos);
        if(entity instanceof BlockEntityElectricFencePole) {
            return ((BlockEntityElectricFencePole) entity).getCachedShape();
        }
        return VoxelShapes.block();
    }



    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        boolean flag = true;
        for (int i = 0; i < this.type.getHeight(); i++) {
            flag &= world.getBlockState(pos.above(i)).getBlock().canBeReplaced(state, Fluids.EMPTY);
        }
        return flag;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(this.indexProperty, 0);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState old, boolean p_220082_5_) {
        if(state.getValue(indexProperty) == 0) {
            for (int i = 1; i < this.type.getHeight(); i++) {
                world.setBlock(pos.above(i), this.defaultBlockState().setValue(indexProperty, i), 3);
            }

        }
        super.onPlace(state, world, pos, old, p_220082_5_);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        int index = state.getValue(indexProperty);
        if(index == 0) {
            ItemStack stack = player.getItemInHand(hand);
            if(stack.isEmpty()) {
                TileEntity te = world.getBlockEntity(pos);
                if(te instanceof BlockEntityElectricFencePole) {
                    ((BlockEntityElectricFencePole) te).setFlippedAround(!((BlockEntityElectricFencePole) te).isFlippedAround());
                    te.setChanged();
                    for (int y = 0; y < this.type.getHeight(); y++) {
                        TileEntity t = world.getBlockEntity(pos.above(y));
                        if(t != null) {
                            t.requestModelDataUpdate();
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            } else if(stack.getItem() == ItemHandler.WIRE_SPOOL.get()) { //Move to item class ?
                CompoundNBT nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
                if(nbt.contains("fence_position", Constants.NBT.TAG_COMPOUND)) {
                    BlockPos other = NBTUtil.readBlockPos(nbt.getCompound("fence_position"));
                    double dist = Math.sqrt(other.distSqr(pos));
                    if(dist > LIMIT) {
                        if(!world.isClientSide) {
                            player.displayClientMessage(new TranslationTextComponent("projectnublar.fences.length.toolong", Math.round(dist), LIMIT), true);
                        }
                        nbt.put("fence_position", NBTUtil.writeBlockPos(pos));
                    } else if(world.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                        int itemMax;
                        int itemAmount = itemMax = Mth.ceil(dist / BlockElectricFence.ITEM_FOLD*this.type.getHeight());
                        int total = 0;
                        boolean full = false;
                        List<Pair<ItemStack, Integer>> stacksFound = Lists.newArrayList();

                        if (itemAmount <= stack.getCount()) {
                            total += itemAmount;
                            stacksFound.add(Pair.of(stack, itemAmount));
                            full = true;
                        } else {
                            total += stack.getCount();
                            stacksFound.add(Pair.of(stack, stack.getCount()));
                        }
                        itemAmount -= stack.getCount();

                        for (ItemStack itemStack : player.inventory.items) {
                            if (itemStack != stack && itemStack.getItem() == ItemHandler.WIRE_SPOOL.get()) {
                                if (itemAmount <= itemStack.getCount()) {
                                    total += itemAmount;
                                    stacksFound.add(Pair.of(itemStack, itemAmount));
                                    full = true;
                                    break;
                                } else {
                                    total += itemStack.getCount();
                                    stacksFound.add(Pair.of(itemStack, itemStack.getCount()));
                                }
                                itemAmount -= itemStack.getCount();
                            }
                        }
                        if(!full) {
                            if(!world.isClientSide) {
                                player.displayClientMessage(new TranslationTextComponent("projectnublar.fences.length.notenough", itemMax, total), true);
                            }
                        } else {
                            if(!player.isCreative()) {
                                stacksFound.forEach(p -> p.getLeft().shrink(p.getRight()));
                            }
                            for (double offset : this.type.getOffsets()) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
                                for (int i = 0; i < this.type.getHeight(); i++) {
                                    BlockPos pos1 = pos.above(i);
                                    BlockPos other1 = other.above(i);
                                    for (int i1 = 0; i1 < positions.size(); i1++) {
                                        BlockPos position = positions.get(i1).above(i);
                                        if (world.getBlockState(position).isAir() || world.getBlockState(position).canBeReplaced(Fluids.EMPTY)) {
                                            world.setBlock(position, BlockHandler.ELECTRIC_FENCE.get().defaultBlockState(), 3);
                                        }
                                        TileEntity fencete = world.getBlockEntity(position);
                                        if (fencete instanceof ConnectableBlockEntity) {
                                            ((ConnectableBlockEntity) fencete).addConnection(new Connection(fencete, this.type, offset, pos1, other1, positions.get(Math.min(i1 + 1, positions.size() - 1)).above(i), positions.get(Math.max(i1 - 1, 0)).above(i), position));
                                        }
                                    }
                                }
                            }
                        }
                        nbt.put("fence_position", NBTUtil.writeBlockPos(pos));
                    } else {
                        nbt.remove("fence_position");
                    }
                } else {
                    nbt.put("fence_position", NBTUtil.writeBlockPos(pos));
                }
                return ActionResultType.SUCCESS;
            }
        } else if (world.getBlockState(pos.below(index)).getBlock() == this) {
            return this.use(world.getBlockState(pos.below(index)), world, pos.below(index), player, hand, ray);
        }
        return super.use(state, world, pos, player, hand, ray);
    }

    @Override
    public void destroy(IWorld world, BlockPos pos, BlockState state) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof BlockEntityElectricFencePole) {
            for (Connection connection : ((BlockEntityElectricFencePole) tileEntity).getConnections()) {
                BlockPos blockpos = connection.getFrom();
                if(blockpos.equals(pos)) {
                    blockpos = connection.getTo();
                }
                if(world.getBlockState(blockpos).getBlock() != this) {
                    for (BlockPos blockPos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                        if(blockPos.equals(connection.getTo()) || blockPos.equals(connection.getFrom())) {
                            continue;
                        }
                        TileEntity te = world.getBlockEntity(blockPos);
                        if(te instanceof ConnectableBlockEntity) {
                            boolean left = false;
                            for (Connection bitcon : ((ConnectableBlockEntity) te).getConnections()) {
                                if(connection.lazyEquals(bitcon)) {
                                    bitcon.setBroken(true);
                                }
                                left |= !bitcon.isBroken();
                            }
                            if(!left) {
                                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }

            }
        }
        super.destroy(world, pos, state);
        if(!destroying) {
            destroying = true;
            int index = state.getValue(indexProperty);
            for (int i = 1; i < index+1; i++) {
                world.setBlock(pos.below(i), Blocks.AIR.defaultBlockState(), 3); //TODO: verify if our block?
            }
            for (int i = 1; i < this.type.getHeight()-index; i++) {
                world.setBlock(pos.above(i), Blocks.AIR.defaultBlockState(), 3);
            }
            destroying = false;
        }
        super.destroy(world, pos, state);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(POWERED_PROPERTY) && state.getValue(indexProperty) == this.type.getHeight()-1 ? this.type.getLightLevel() : 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockEntityElectricFencePole();
    }
}
