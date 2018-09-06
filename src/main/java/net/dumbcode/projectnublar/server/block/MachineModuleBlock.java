package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineModuleBlock<I extends Predicate<ItemStack> & IStringSerializable> extends Block implements IItemBlock{

    private final I[] values; //Not needed
    private final Map<I, PropertyBool> propertyMap = Maps.newHashMap();
    private final Supplier<? extends MachineModuleBlockEntity> machineSupplier;
    private final BlockStateContainer blockState;

    public MachineModuleBlock(I[] values, Supplier<? extends MachineModuleBlockEntity> machineSupplier) {
        super(Material.IRON);
        this.values = values;
        this.machineSupplier = machineSupplier;

        for (I value : this.values) {
            this.propertyMap.put(value, PropertyBool.create(value.getName()));
        }

        this.blockState = new BlockStateContainer(this, this.propertyMap.values().toArray(new IProperty[0])); //Needed as the container making is usually created in the block constructor, so the propertyMap values arn't set correctly (the map is null)
        IBlockState baseState = this.blockState.getBaseState();
        for (I value : this.values) {
            baseState = baseState.withProperty(this.propertyMap.get(value), false);
        }
        this.setDefaultState(baseState);
    }

    public int getStateID(IBlockState state) {
        int out = 0;
        for (int i = 0; i < this.values.length; i++) {
            if(state.getValue(this.propertyMap.get(this.values[i]))) {
                out |= (int) Math.pow(2, i);
            }
        }
        return out;
    }

    public IBlockState getStateFromID(int id) {
        IBlockState state = this.getDefaultState();
        for (int i = 0; i < this.values.length; i++) {
            state = state.withProperty(this.propertyMap.get(this.values[i]), (id & (int) Math.pow(2, i)) != 0);
        }
        return state;
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
            MachineModuleBlockEntity blockEntity = (MachineModuleBlockEntity) tileEntity;
            int stateID = blockEntity.getStateID();
            boolean valid = true;
            for (int i = 0; i < this.values.length; i++) {
                int mask = (int) Math.pow(2, i);
                if((stateID & mask) == 0) {
//                    valid = false;
                    if(this.values[i].test(stack)) {
                        blockEntity.setStateID(stateID | mask);
                        stack.shrink(1);
                        if(worldIn.isRemote) {
                            worldIn.markBlockRangeForRenderUpdate(pos, pos);
                        }
                        blockEntity.markDirty();
                        break;
                    }
                }
            }
            if(valid) {
                playerIn.openGui(ProjectNublar.INSTANCE, -1/*Not currently used. TODO: use*/, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof MachineModuleBlockEntity) {
            return this.getStateFromID(((MachineModuleBlockEntity)tileEntity).getStateID());
        }
        return super.getActualState(state, worldIn, pos);
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
