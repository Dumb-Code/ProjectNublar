package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

//TODO: tint the item model fossil overlay
public class FossilItem extends BlockItem {
    StoneType stone;
    Fossil fossil;
    public FossilItem(Block block, Properties properties, Fossil fossil, StoneType stoneType) {
        super(block, properties);
    }
}
