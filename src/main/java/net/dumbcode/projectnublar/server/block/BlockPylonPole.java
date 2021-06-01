package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.ItemPylonPole;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockPylonPole extends Block implements IItemBlock {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, axis -> axis != null && axis.isHorizontal());

    public BlockPylonPole(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        IBlockState s = worldIn.getBlockState(pos.down());
        if(s.getBlock() != this && !s.isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            worldIn.setBlockToAir(pos);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if(stack.getItem() == ItemHandler.WIRE_SPOOL) {
            BlockPos testPos = pos;
            while(worldIn.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE) {
                testPos = testPos.up();
            }
            IBlockState head = worldIn.getBlockState(testPos);
            if(head.getBlock() == BlockHandler.PYLON_HEAD && head.getValue(BlockPylonHead.FACING) == EnumFacing.UP) {
                head.getBlock().onBlockActivated(worldIn, testPos, head, playerIn, hand, facing, -1, -1, -1);
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        BlockPos testPos = pos;
        while(world.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE) {
            testPos = testPos.up();
        }
        IBlockState head = world.getBlockState(testPos);
        if(head.getBlock() == BlockHandler.PYLON_HEAD && head.getValue(BlockPylonHead.FACING) == EnumFacing.UP) {
            TileEntity old = world.getTileEntity(testPos);
            if(!world.isRemote && old instanceof PylonHeadBlockEntity) {
                if(!player.isCreative()) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(BlockHandler.PYLON_POLE));
                }
                world.setBlockToAir(testPos);
                world.setBlockState(testPos.down(), head);
                ItemPylonPole.moveConnections((PylonHeadBlockEntity) old, testPos.down());
            }
            return true;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AXIS).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AXIS, EnumFacing.Axis.values()[meta]);
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
    public Item createItem() {
        return new ItemPylonPole();
    }

}
