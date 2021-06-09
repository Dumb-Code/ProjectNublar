package net.dumbcode.projectnublar.server.block;

import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class SkeletalBuilderBlock extends DirectionalBlock implements IItemBlock {

    public static final TranslationTextComponent NO_DINOSAUR_TO_DISPLAY_TEXT = new TranslationTextComponent(ProjectNublar.MODID+".action.skeletal_builder.no_dino_to_display");

    protected SkeletalBuilderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        ItemStack stack = player.getItemInHand(hand);
        if(tileEntity instanceof SkeletalBuilderBlockEntity) {
            SkeletalBuilderBlockEntity sb = (SkeletalBuilderBlockEntity) tileEntity;
            if(stack.getItem() instanceof FossilItem) {
                FossilItem item = (FossilItem)stack.getItem();
                Dinosaur dinosaur = item.getDinosaur();
                if(!sb.getDinosaur().isPresent()) {
                    sb.setDinosaur(dinosaur);
                }
                this.setBonesInHandler(sb, dinosaur, stack, item.getVariant());
                return ActionResultType.SUCCESS;
            } else if(stack.isEmpty()) {
                if (sb.getDinosaur().isPresent()) {
                    if(!world.isClientSide) {
                        TaxidermyContainer.open((ServerPlayerEntity) player, sb);
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    player.displayClientMessage(NO_DINOSAUR_TO_DISPLAY_TEXT, true);
                    return ActionResultType.FAIL;
                }
            }
        }
        return super.use(state, world, pos, player, hand, ray);
    }

//    @SideOnly(Side.CLIENT)
//    private void displayGui(SkeletalBuilderBlockEntity sb) {
//        TextComponentTranslation title = new TextComponentTranslation(ProjectNublar.MODID + ".gui.model_pose_edit.title", sb.getDinosaur().orElse(DinosaurHandler.TYRANNOSAURUS).createNameComponent().getUnformattedText());
//        Minecraft.getMinecraft().displayGuiScreen(new GuiTaxidermy(sb.getModel(), sb.getTexture(), title, sb));
//    }

    private void setBonesInHandler(SkeletalBuilderBlockEntity skeletalBuilder, Dinosaur dinosaur, ItemStack stack, String variant) {
        if(skeletalBuilder.getDinosaur().map(d -> d == dinosaur).orElse(false) && skeletalBuilder.getDinosaurEntity().isPresent()) {
            skeletalBuilder.getDinosaurEntity().flatMap(ComponentHandler.SKELETAL_BUILDER.get()).ifPresent(component -> {
                List<String> boneList = component.getBoneListed();
                if (component.modelIndex < boneList.size() && variant.equals(boneList.get(component.modelIndex))) {
                    skeletalBuilder.getBoneHandler().setStackInSlot(component.modelIndex++, stack.split(1));
                }
            });
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState p_149645_1_) {
        return BlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean doDrops) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SkeletalBuilderBlockEntity) {
            ItemStackHandler itemHandler = ((SkeletalBuilderBlockEntity)tileentity).getBoneHandler();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemHandler.getStackInSlot(i));
            }
        }
        super.onRemove(state, world, pos, state, doDrops);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SkeletalBuilderBlockEntity();
    }
}