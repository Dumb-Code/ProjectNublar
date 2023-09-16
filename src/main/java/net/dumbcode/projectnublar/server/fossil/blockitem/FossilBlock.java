package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Block;

public class FossilBlock extends Block {
    public Fossil fossil;
    public StoneType stone;
    public FossilBlock(Properties properties, Fossil fossil, StoneType stone) {
        super(properties);
    }
}
