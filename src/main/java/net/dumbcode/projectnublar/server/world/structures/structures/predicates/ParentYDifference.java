package net.dumbcode.projectnublar.server.world.structures.structures.predicates;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;

@RequiredArgsConstructor
public class ParentYDifference implements StructurePredicate {
    private final ValueRange range;

    @Override
    public boolean canBuildDirect(StructureInstance instance) {
        return instance.getParent() == null || this.range.inRange(instance.getPosition().getY() - instance.getParent().getPosition().getY());
    }
}
