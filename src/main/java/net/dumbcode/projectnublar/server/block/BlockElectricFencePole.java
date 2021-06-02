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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockAccess;
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
    

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        double rotation = (state.getActualState(source, pos).getValue(ROTATION_PROPERTY) + (90 - this.type.getRotationOffset())) / 180D * Math.PI;
        float t = this.type.getHalfSize();
        double x = Math.sin(rotation) * this.type.getRadius();
        double z = Math.cos(rotation) * this.type.getRadius();
        return new AxisAlignedBB(x-t, 0, z-t, x+t, 1, z+t).offset(0.5, 0, 0.5);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult trace = this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
        if(trace != null) {
            return trace;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity && ((ConnectableBlockEntity) te).getConnections().isEmpty()) {
            return null;
        }
        return super.collisionRayTrace(blockState, worldIn, pos, start, end);
    }

    @Override
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        boolean flag = true;
        for (int i = 0; i < this.type.getHeight(); i++) {
            flag &= worldIn.getBlockState(pos.up(i)).getBlock().isReplaceable(worldIn, pos.up(i));
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
            } else if(stack.getItem() == ItemHandler.WIRE_SPOOL) { //Move to item class ?
                CompoundNBT nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
                if(nbt.contains("fence_position", Constants.NBT.TAG_LONG)) {
                    BlockPos other = BlockPos.of(nbt.getLong("fence_position"));
                    double dist = Math.sqrt(other.distSqr(pos));
                    if(dist > LIMIT) {
                        if(!world.isClientSide) {
                            player.displayClientMessage(new TranslationTextComponent("projectnublar.fences.length.toolong", Math.round(dist), LIMIT), true);
                        }
                        nbt.putLong("fence_position", pos.asLong());
                    } else if(world.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                        int itemMax;
                        int itemAmount = itemMax = MathHelper.ceil(dist / BlockElectricFence.ITEM_FOLD*this.type.getHeight());
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

                        for (ItemStack itemStack : player.inventory.mainInventory) {
                            if (itemStack != stack && itemStack.getItem() == ItemHandler.WIRE_SPOOL) {
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
                            if(!world.isRemote) {
                                player.sendStatusMessage(new TextComponentTranslation("projectnublar.fences.length.notenough", itemMax, total), true);
                            }
                        } else {
                            if(!player.isCreative()) {
                                stacksFound.forEach(p -> p.getLeft().shrink(p.getRight()));
                            }
                            for (double offset : this.type.getOffsets()) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
                                for (int i = 0; i < this.type.getHeight(); i++) {
                                    BlockPos pos1 = pos.up(i);
                                    BlockPos other1 = other.up(i);
                                    for (int i1 = 0; i1 < positions.size(); i1++) {
                                        BlockPos position = positions.get(i1).up(i);
                                        if (world.isAirBlock(position) || world.getBlockState(position).getBlock().isReplaceable(world, position)) {
                                            world.setBlockState(position, BlockHandler.ELECTRIC_FENCE.getDefaultState());
                                        }
                                        TileEntity fencete = world.getTileEntity(position);
                                        if (fencete instanceof ConnectableBlockEntity) {
                                            ((ConnectableBlockEntity) fencete).addConnection(new Connection(this.type, offset, pos1, other1, positions.get(Math.min(i1 + 1, positions.size() - 1)).up(i), positions.get(Math.max(i1 - 1, 0)).up(i), position));
                                            fencete.markDirty();
                                            world.notifyBlockUpdate(position, world.getBlockState(position), world.getBlockState(position), 3);
                                        }
                                    }
                                }
                            }
                        }
                        nbt.setLong("fence_position", pos.toLong());
                    } else {
                        nbt.removeTag("fence_position");
                    }
                } else {
                    nbt.setLong("fence_position", pos.toLong());
                }
                return true;
            }
        } else if (world.getBlockState(pos.down(index)).getBlock() == this) {
            return this.onBlockActivated(world, pos.down(index), world.getBlockState(pos.down(index)), player, hand, facing, hitX, hitY, hitZ);
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof BlockEntityElectricFencePole) {
            for (Connection connection : ((BlockEntityElectricFencePole) tileEntity).getConnections()) {
                BlockPos blockpos = connection.getFrom();
                if(blockpos.equals(pos)) {
                    blockpos = connection.getTo();
                }
                if(worldIn.getBlockState(blockpos).getBlock() != this) {
                    for (BlockPos blockPos : LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset())) {
                        if(blockPos.equals(connection.getTo()) || blockPos.equals(connection.getFrom())) {
                            continue;
                        }
                        TileEntity te = worldIn.getTileEntity(blockPos);
                        if(te instanceof ConnectableBlockEntity) {
                            boolean left = false;
                            for (Connection bitcon : ((ConnectableBlockEntity) te).getConnections()) {
                                if(connection.lazyEquals(bitcon)) {
                                    bitcon.setBroken(true);
                                }
                                left |= !bitcon.isBroken();
                            }
                            if(!left) {
                                worldIn.setBlockToAir(blockPos);
                            }
                        }
                    }
                }

            }
        }
        super.breakBlock(worldIn, pos, state);
        if(!destroying) {
            destroying = true;
            int index = state.getValue(indexProperty);
            for (int i = 1; i < index+1; i++) {
                worldIn.setBlockToAir(pos.down(i)); //TODO: verify if our block?
            }
            for (int i = 1; i < this.type.getHeight()-index; i++) {
                worldIn.setBlockToAir(pos.up(i));
            }
            destroying = false;
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(POWERED_PROPERTY) && state.getValue(indexProperty) == this.type.getHeight()-1 ? this.type.getLightLevel() : 0;
    }

    @Override
    public int getLightOpacity(IBlockState state) {
        return super.getLightOpacity(state);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(indexProperty, meta);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(indexProperty);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntityElectricFencePole();
    }
}
