package net.dumbcode.projectnublar.server.world.structures.structures.predicates;


import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HeightRangePredicate implements StructurePredicate {

    private final ValueRange range;

    @Override
    public void setupTraversers(Consumer<PredicateTraverser<?>> registry) {
        registry.accept(new PredicateTraverser<>(
            Pair.of(new AtomicInteger(Integer.MAX_VALUE), new AtomicInteger(Integer.MIN_VALUE)), //min, max
            (instance, pos, pair) -> {
                pair.getLeft().getAndAccumulate(pos.getY(), Math::min);
                pair.getRight().getAndAccumulate(pos.getY(), Math::max);
            }, pair -> this.range.inRange(pair.getRight().doubleValue() - pair.getLeft().doubleValue()))
        );
    }
}
