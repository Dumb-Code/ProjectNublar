package net.dumbcode.projectnublar.server.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public interface IItemBlock {
    default Item createItem(Item.Properties properties) {
        return new BlockItem((Block) this, properties);
    }
}
