package net.dumbcode.projectnublar.server.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public interface IItemBlock {
    default Item createItem() {
        return new ItemBlock((Block)this);
    }
}
