package net.dumbcode.projectnublar.server.world.structures.structures.template.placement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TemplatePlacement {
    BlockPos transpose(World world, BlockPos worldPosition, BlockPos relativePosition);
}
