package net.dumbcode.projectnublar.server.block;

import lombok.Getter;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//TODO: move to a simple classs
public class FossilBlock extends Block implements DinosaurProvider, IItemBlock {

    @Getter private final Dinosaur dinosaur;

    @Getter private final FossilType fossilType;

    public FossilBlock(Dinosaur dinosaur, FossilType fossilType) {
        super(Material.ROCK);
        this.dinosaur = dinosaur;
        this.fossilType = fossilType;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Getter
    public enum FossilType {
        STONE("stone"), SANDSTONE("sandstone"), CLAY("clay");

        private final String name;

        FossilType(String name) {
            this.name = name;
        }

        public static IBlockState guess(IBlockState state, Dinosaur dino) {
            if(state.getBlock() == Blocks.HARDENED_CLAY || state.getBlock() == Blocks.STAINED_HARDENED_CLAY) {
                return BlockHandler.FOSSIL.get(CLAY).get(dino).getDefaultState();
            }
            if(state.getBlock() == Blocks.SANDSTONE || state.getBlock() == Blocks.SAND) {
                return BlockHandler.FOSSIL.get(SANDSTONE).get(dino).getDefaultState();
            }
            return BlockHandler.FOSSIL.get(STONE).get(dino).getDefaultState();
        }
    }


}
