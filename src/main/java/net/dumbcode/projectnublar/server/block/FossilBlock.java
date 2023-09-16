package net.dumbcode.projectnublar.server.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.data.ProjectNublarBlockTags;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

//TODO: move to a simple classs

/**
 * For removal soon
 * wtf is this, obsolete with the new system
 */
@Deprecated
public class FossilBlock extends Block implements IItemBlock {

    @Getter private final Dinosaur dinosaur;

    @Getter private final FossilType fossilType;

    public FossilBlock(Dinosaur dinosaur, FossilType fossilType, Properties properties) {
        super(properties);
        this.dinosaur = dinosaur;
        this.fossilType = fossilType;
    }

    @Getter
    @RequiredArgsConstructor
    public enum FossilType {
        STONE("stone", Blocks.STONE),
        SANDSTONE("sandstone", Blocks.SANDSTONE),
        CLAY("clay", Blocks.CLAY);

        private final String name;
        private final Block copy;

        public static BlockState guess(BlockState state, Dinosaur dino) {
            if(state.is(ProjectNublarBlockTags.FOSSIL_CLAY_REPLACEMENT)) {
                return BlockHandler.FOSSIL.get(CLAY).get(dino).defaultBlockState();
            }
            if(state.is(ProjectNublarBlockTags.FOSSIL_SANDSTONE_REPLACEMENT)) {
                return BlockHandler.FOSSIL.get(SANDSTONE).get(dino).defaultBlockState();
            }
            return BlockHandler.FOSSIL.get(STONE).get(dino).defaultBlockState();
        }
    }


}
