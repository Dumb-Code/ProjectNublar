package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.FossilBlockEntity;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;

public class FossilBlockItem extends BlockItem {

    public FossilBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
        if (this.allowdedIn(pGroup)) {
            FossilHandler.FOSSIL_REGISTRY.get().getValues().forEach(fossil ->
                    fossil.getStoneTypes().forEach(stoneType -> pItems.add(setFossilStack(fossil, stoneType)))
            );
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pPos, World pLevel, @Nullable PlayerEntity pPlayer, ItemStack pStack, BlockState pState) {
        if (super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState)) {
            return true;
        }
        Fossil fossil = getFossil(pStack);
        StoneType stoneType = getStoneType(pStack);

        TileEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FossilBlockEntity) {
            FossilBlockEntity fbe = (FossilBlockEntity) blockEntity;
            fbe.setFossil(fossil);
            fbe.setStoneType(stoneType);

            return true;
        }

        return false;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        StoneType stone = getStoneType(stack);
        Fossil fossil = getFossil(stack);
        return new TranslationTextComponent("projectnublar.fossil", stone.baseState.get().getBlock().getName(), WordUtils.capitalizeFully(fossil.name));
    }

    public static ItemStack setFossilStack(Fossil fossil, StoneType stoneType) {
        return setFossilStack(new ItemStack(BlockHandler.FOSSIL_BLOCK.get()), fossil, stoneType);
    }

    public static ItemStack setFossilStack(ItemStack base, Fossil fossil, StoneType stoneType) {
        CompoundNBT nbt = base.getOrCreateTagElement(ProjectNublar.MODID);

        nbt.putString("fossil", fossil.getRegistryName().toString());
        nbt.putString("stone_type", stoneType.getRegistryName().toString());

        return base;
    }

    public static Fossil getFossil(ItemStack base) {
        CompoundNBT nbt = base.getOrCreateTagElement(ProjectNublar.MODID);
        return FossilHandler.FOSSIL_REGISTRY.get().getValue(new ResourceLocation(nbt.getString("fossil")));
    }

    public static StoneType getStoneType(ItemStack base) {
        CompoundNBT nbt = base.getOrCreateTagElement(ProjectNublar.MODID);
        return StoneTypeHandler.STONE_TYPE_REGISTRY.get().getValue(new ResourceLocation(nbt.getString("stone_type")));
    }
}
