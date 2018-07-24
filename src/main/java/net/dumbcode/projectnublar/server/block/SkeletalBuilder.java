package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class SkeletalBuilder extends BlockDirectional implements IItemBlock {

    public SkeletalBuilder() {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        ItemStack stack = playerIn.getHeldItem(hand);
        if(tileEntity instanceof BlockEntitySkeletalBuilder) {
            BlockEntitySkeletalBuilder skeletalBuilder = (BlockEntitySkeletalBuilder) tileEntity;
            if(stack.getItem() == ItemHandler.FOSSIL) {
                FossilItem.FossilInfomation info = FossilItem.getFossilInfomation(stack);
                if(info.getDinosaur() == skeletalBuilder.getDinosaur()) {
                    DinosaurEntity entity = skeletalBuilder.getDinosaurEntity();
                    List<String> boneList = info.getDinosaur().getSkeletalInfomation().getBoneListed();
                    if(entity.modelIndex < boneList.size()) {
                        if(info.getType().equals(boneList.get(entity.modelIndex))) {
                            entity.modelIndex++;
                            stack.shrink(1);
                        }
                    }
                }
            } else if(playerIn.isSneaking()) {
                skeletalBuilder.setRotation(Rotation.values()[(skeletalBuilder.getRotation().ordinal() + 1) % Rotation.values().length]);
            }
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
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
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta % EnumFacing.values().length]);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntitySkeletalBuilder();
    }
}