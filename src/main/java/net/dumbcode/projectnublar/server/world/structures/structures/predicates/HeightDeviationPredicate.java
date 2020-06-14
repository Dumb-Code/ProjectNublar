package net.dumbcode.projectnublar.server.world.structures.structures.predicates;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.utils.ValueRange;

import java.util.ArrayList;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HeightDeviationPredicate implements StructurePredicate {

    private final ValueRange range;

    @Override
    public void setupTraversers(Consumer<PredicateTraverser<?>> registry) {
        registry.accept(new PredicateTraverser<>(
            new ArrayList<Integer>(),
            (instance, pos, ints) -> ints.add(pos.getY()),
            integers -> this.range.inRange(MathUtils.meanDeviation(integers))))
        ;
    }
}
