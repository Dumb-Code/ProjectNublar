package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.gui.GuiHandler;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class SkeletalBuilder extends BlockDirectional implements IItemBlock {

    public static final TextComponentTranslation NO_DINOSAUR_TO_DISPLAY_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".action.skeletal_builder.no_dino_to_display");

    public SkeletalBuilder() {
        super(Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        ItemStack stack = playerIn.getHeldItem(hand);
        if(tileEntity instanceof BlockEntitySkeletalBuilder) {
            BlockEntitySkeletalBuilder skeletalBuilder = (BlockEntitySkeletalBuilder) tileEntity;
            if(stack.getItem() instanceof FossilItem) {
                FossilItem item = (FossilItem)stack.getItem();
                Dinosaur dinosaur = item.getDinosaur();
                String varient = item.getVarient();
                if(skeletalBuilder.getDinosaur() == Dinosaur.MISSING) {
                    skeletalBuilder.setDinosaur(dinosaur);
                }
                if(dinosaur == skeletalBuilder.getDinosaur()) {
                    DinosaurEntity entity = skeletalBuilder.getDinosaurEntity();
                    List<String> boneList = dinosaur.getSkeletalInformation().getBoneListed();
                    if(entity.modelIndex < boneList.size()) {
                        if(varient.equals(boneList.get(entity.modelIndex))) {
                            skeletalBuilder.getBoneHandler().setStackInSlot(entity.modelIndex++, stack.splitStack(1));
                        }
                    }
                }
            } else if(playerIn.getHeldItem(hand).isEmpty()) {
                if(skeletalBuilder.getDinosaur() == Dinosaur.MISSING) {
                    playerIn.sendStatusMessage(NO_DINOSAUR_TO_DISPLAY_TEXT, true);
                } else {
                    playerIn.openGui(ProjectNublar.INSTANCE, GuiHandler.SKELETAL_BUILDER_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, facing);
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof BlockEntitySkeletalBuilder) {
            ItemStackHandler itemHandler = ((BlockEntitySkeletalBuilder)tileentity).getBoneHandler();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemHandler.getStackInSlot(i));
            }
        }

        super.breakBlock(worldIn, pos, state);
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