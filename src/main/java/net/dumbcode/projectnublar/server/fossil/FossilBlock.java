package net.dumbcode.projectnublar.server.fossil;

import net.minecraft.block.Block;

public class FossilBlock extends Block {
    Fossil fossil;
    StoneType stone;
    public FossilBlock(Properties properties, Fossil fossil, StoneType stone) {
        super(properties);
    }
}
