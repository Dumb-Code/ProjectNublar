package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.network.NetworkUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.MachineModulePart;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.network.S2CMachinePositionDirty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MachineModuleBlock extends HorizontalBlock implements IItemBlock {

    private final List<MachineModulePart> values; //Not needed
    private final Map<MachineModuleType, IntegerProperty> propertyMap = Maps.newHashMap();
    private final Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier;
    private final StateContainer<Block, BlockState> stateDefinition;


    public MachineModuleBlock(Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier, MachineModulePart[] values, Properties properties) {
        super(properties);
        this.values = Lists.newArrayList(values);
        this.machineSupplier = machineSupplier;

        for (MachineModulePart part : this.values) {
            this.propertyMap.put(part.getType(), IntegerProperty.create(part.getName(), 0, part.getTiers()));
        }

        //Needed as the container making is usually created in the block constructor, so the propertyMap values arn't set correctly (the map is null)
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(this.propertyMap.values().toArray(new Property<?>[0]));
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);

        BlockState baseState = this.stateDefinition.any();
        for (MachineModulePart part : this.values) {
            baseState = baseState.setValue(this.propertyMap.get(part.getType()), 0);
        }

        this.registerDefaultState(baseState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        if(world instanceof ServerWorld) {
            ProjectNublar.NETWORK.send(NetworkUtils.forPos(world, pos), new S2CMachinePositionDirty(pos));
        }
    }

    @Override
    public StateContainer<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    public Map<MachineModuleType, IntegerProperty> getPropertyMap() {
        return propertyMap;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            MachineModuleBlockEntity<?> blockEntity = (MachineModuleBlockEntity) tileEntity;
            for (MachineModulePart value : this.values) {
                int tier = blockEntity.getTier(value.getType());
                int newTier = value.getTierFromStack(stack);
                if(newTier == tier+1 && value.testDependents(newTier, blockEntity::getTier)) {
                    world.setBlock(pos, state.setValue(this.propertyMap.get(value.getType()), newTier), 3);
                    blockEntity.tiersUpdated();
                    stack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }
            if(player instanceof ServerPlayerEntity) {
                blockEntity.openContainer((ServerPlayerEntity) player, 0);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void destroy(IWorld world, BlockPos pos, BlockState state) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            ((MachineModuleBlockEntity<?>) tileEntity).dropEmStacks();
        }
        super.destroy(world, pos, state);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return this.machineSupplier.get();
    }

}
