package net.dumbcode.projectnublar.server.world.structures.structures.predicates;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.utils.ValueRange;
import net.minecraft.block.BlockLeaves;
import net.minecraft.init.Blocks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SolidLiquidRatioPredicate implements StructurePredicate {
    private final ValueRange liquidRange;
    private final ValueRange solidRange;

    @Override
    public void setupTraversers(Consumer<PredicateTraverser> registry) {
        registry.accept(new PredicateTraverser<>(
                Pair.of(new AtomicInteger(), new AtomicInteger()), //solids, liquids
                (instance, pos, pair) -> {
                    if(instance.getWorld().getBlockState(pos.down()).getMaterial().isLiquid()) {
                        pair.getRight().incrementAndGet();
                    } else {
                        pair.getLeft().incrementAndGet();
                    }
                }, pair -> {
                    double total = pair.getLeft().doubleValue() + pair.getRight().doubleValue();
                    return this.liquidRange.inRange(pair.getRight().get() / total) && this.solidRange.inRange(pair.getLeft().get() / total);
            })
        );
    }
}
