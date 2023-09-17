package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.text.WordUtils;

//TODO: tint the item model fossil overlay
public class FossilItem extends BlockItem {
    StoneType stone;
    Fossil fossil;
    public FossilItem(Block block, Properties properties, Fossil fossil, StoneType stone) {
        super(block, properties);
        this.fossil = fossil;
        this.stone = stone;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new TranslationTextComponent("projectnublar.fossil", WordUtils.capitalizeFully(stone.name), WordUtils.capitalizeFully(fossil.name));
    }
}
