package net.dumbcode.projectnublar.server.world.gen.trees;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.server.utils.GaussianValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

@Wither
@AllArgsConstructor
public class RedwoodTreeGenerator extends WorldGenerator {

    private GaussianValue stemSize = new GaussianValue(7, 1);

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        return false;
    }

    private void generateStem(World world, BlockPos pos, Random random) {

    }
}
