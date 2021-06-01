package net.dumbcode.projectnublar.server.block;

import net.dumbcode.dumblibrary.client.gui.GuiTaxidermy;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SkeletalBuilderBlock extends DirectionalBlock implements IItemBlock {

    public static final TextComponentTranslation NO_DINOSAUR_TO_DISPLAY_TEXT = new TextComponentTranslation(ProjectNublar.MODID+".action.skeletal_builder.no_dino_to_display");

    protected SkeletalBuilderBlock(Properties properties) {
        super(properties);
    }


    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        ItemStack stack = playerIn.getHeldItem(hand);
        if(tileEntity instanceof SkeletalBuilderBlockEntity) {
            SkeletalBuilderBlockEntity sb = (SkeletalBuilderBlockEntity) tileEntity;
            if(stack.getItem() instanceof FossilItem) {
                FossilItem item = (FossilItem)stack.getItem();
                Dinosaur dinosaur = item.getDinosaur();
                if(!sb.getDinosaur().isPresent()) {
                    sb.setDinosaur(dinosaur);
                }
                this.setBonesInHandler(sb, dinosaur, stack, item.getVariant());
            } else if(playerIn.getHeldItem(hand).isEmpty()) {
                if (sb.getDinosaur().isPresent()) {
                    if(worldIn.isRemote) {
                        displayGui(sb);
                    }
                } else {
                    playerIn.sendStatusMessage(NO_DINOSAUR_TO_DISPLAY_TEXT, true);
                }
            }
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    private void displayGui(SkeletalBuilderBlockEntity sb) {
        TextComponentTranslation title = new TextComponentTranslation(ProjectNublar.MODID + ".gui.model_pose_edit.title", sb.getDinosaur().orElse(DinosaurHandler.TYRANNOSAURUS).createNameComponent().getUnformattedText());
        Minecraft.getMinecraft().displayGuiScreen(new GuiTaxidermy(sb.getModel(), sb.getTexture(), title, sb));
    }

    private void setBonesInHandler(SkeletalBuilderBlockEntity skeletalBuilder, Dinosaur dinosaur, ItemStack stack, String variant) {
        if(skeletalBuilder.getDinosaur().map(d -> d == dinosaur).orElse(false) && skeletalBuilder.getDinosaurEntity().isPresent()) {
            skeletalBuilder.getDinosaurEntity().flatMap(ComponentHandler.SKELETAL_BUILDER).ifPresent(component -> {
                List<String> boneList = component.getBoneListed();
                if (component.modelIndex < boneList.size() && variant.equals(boneList.get(component.modelIndex))) {
                    skeletalBuilder.getBoneHandler().setStackInSlot(component.modelIndex++, stack.splitStack(1));
                }
            });
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, facing);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SkeletalBuilderBlockEntity) {
            ItemStackHandler itemHandler = ((SkeletalBuilderBlockEntity)tileentity).getBoneHandler();
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
        return new SkeletalBuilderBlockEntity();
    }
}