package net.dumbcode.projectnublar.server.world.structures.structures.placement;

import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PushdownPlacement implements StructurePlacement {

    private final int levelY;

    public PushdownPlacement(int levelY) {
        this.levelY = levelY;
    }

    @Override
    public BlockPos transpose(World world, StructureInstance instance, BlockPos worldPosition, BlockPos relativePosition) {
        return WorldUtils.getDirectTopdownBlock(world, worldPosition).up(relativePosition.getY() - this.levelY);
    }
}
