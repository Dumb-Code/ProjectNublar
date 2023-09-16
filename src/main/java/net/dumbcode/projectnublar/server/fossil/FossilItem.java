package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

//TODO: tint the item model fossil overlay
public class FossilItem extends BlockItem {
    StoneType type;
    Fossil fossil;
    public FossilItem(Block block, Properties properties, Fossil fossil, StoneType stoneType) {
        super(block, properties);
    }
}
