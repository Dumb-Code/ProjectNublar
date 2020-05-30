package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.MachineModulePart;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.network.S17MachinePositionDirty;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MachineModuleBlock extends Block implements IItemBlock{

    private final List<MachineModulePart> values; //Not needed
    private final Map<MachineModuleType, PropertyInteger> propertyMap = Maps.newHashMap();
    private final Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier;
    private final BlockStateContainer blockState;
    private final Set<BlockRenderLayer> renderLayers;

    public MachineModuleBlock(Supplier<? extends MachineModuleBlockEntity<?>> machineSupplier, MachineModulePart[] values, BlockRenderLayer... renderLayers) {
        super(Material.IRON);
        this.renderLayers = Sets.newHashSet(renderLayers);
        if(this.renderLayers.isEmpty()) {
            this.renderLayers.add(BlockRenderLayer.SOLID);
        }
        this.values = Lists.newArrayList(values);
        this.machineSupplier = machineSupplier;

        for (MachineModulePart part : this.values) {
            this.propertyMap.put(part.getType(), PropertyInteger.create(part.getName(), 0, part.getTiers()));
        }

        //Needed as the container making is usually created in the block constructor, so the propertyMap values arn't set correctly (the map is null)
        this.blockState = new BlockStateContainer(this, this.propertyMap.values().toArray(new IProperty[0]));
        IBlockState baseState = this.blockState.getBaseState();
        for (MachineModulePart part : this.values) {
            baseState = baseState.withProperty(this.propertyMap.get(part.getType()), 0);
        }
        this.setDefaultState(baseState);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        ProjectNublar.NETWORK.sendToDimension(new S17MachinePositionDirty(pos), worldIn.provider.getDimension());
    }

    @Override
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        ItemStack stack = playerIn.getHeldItem(hand);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            MachineModuleBlockEntity<?> blockEntity = (MachineModuleBlockEntity) tileEntity;
            for (MachineModulePart value : this.values) {
                int tier = blockEntity.getTier(value.getType());
                int newTier = value.getTierFromStack(stack);
                if(newTier == tier+1 && value.testDependents(newTier, blockEntity::getTier)) {
                    blockEntity.setTier(value.getType(), newTier);
                    blockEntity.tiersUpdated();
                    stack.shrink(1);
                    worldIn.notifyBlockUpdate(pos, state, state, 3);
                    blockEntity.markDirty();
                    return true;
                }
            }
            playerIn.openGui(ProjectNublar.INSTANCE, 0/*Not currently used. TODO: use*/, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return this.renderLayers.contains(layer);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            for (MachineModulePart part : this.values) {
                state = state.withProperty(this.propertyMap.get(part.getType()), ((MachineModuleBlockEntity) tileEntity).getTier(part.getType()));
            }
            return state;
        }
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            ((MachineModuleBlockEntity<?>) tileEntity).dropEmStacks();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return this.machineSupplier.get();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
}
