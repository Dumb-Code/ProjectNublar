package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.block.entity.FossilBlockEntity;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FossilBlock extends Block {
    public FossilBlock(Properties properties) {
        super(properties);
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FossilBlockEntity();
    }

    // Methods to delegate to the base state:
    // TODO (wp): make this get it from the block entity
//    @Override
//    public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
//        return this.stone.baseState.get().getShape(pLevel, pPos);
//    }
//
//    @Override
//    public int getHarvestLevel(BlockState state) {
//        return this.stone.baseState.get().getHarvestLevel();
//    }
//
//    @Nullable
//    @Override
//    public ToolType getHarvestTool(BlockState state) {
//        return this.stone.baseState.get().getHarvestTool();
//    }
//
//    @Override
//    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
//        return this.stone.baseState.get().getSoundType(world, pos, entity);
//    }

}
