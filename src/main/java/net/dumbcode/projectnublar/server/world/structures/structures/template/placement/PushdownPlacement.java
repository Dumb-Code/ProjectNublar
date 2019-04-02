package net.dumbcode.projectnublar.server.world.structures.structures.template.placement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PushdownPlacement implements TemplatePlacement {

    private final int levelY;

    public PushdownPlacement(int levelY) {
        this.levelY = levelY;
    }

    @Override
    public BlockPos transpose(World world, BlockPos worldPosition, BlockPos relativePosition) {
        return world.getTopSolidOrLiquidBlock(worldPosition).up(relativePosition.getY() - this.levelY);
    }
}
