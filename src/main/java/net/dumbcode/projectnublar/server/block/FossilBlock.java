package net.dumbcode.projectnublar.server.block;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

//TODO: move to a simple classs
public class FossilBlock extends Block implements DinosaurProvider, IItemBlock {

    @Getter
    private final Dinosaur dinosaur;

    public FossilBlock(Dinosaur dinosaur) {
        super(Material.ROCK);
        this.dinosaur = dinosaur;
    }
}
