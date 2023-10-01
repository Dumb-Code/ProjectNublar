package net.dumbcode.projectnublar.server.world.structures.structures.predicates;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.world.structures.StructureInstance;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Function;

@RequiredArgsConstructor
public class PredicateTraverser<A> {
    private final A accumulator;
    private final TriConsumer<StructureInstance, BlockPos, A> onBlockPosition;
    private final Function<A, Boolean> finisher;

    public void onTraverse(StructureInstance instance, BlockPos pos) {
        this.onBlockPosition.accept(instance, pos, this.accumulator);
    }

    public boolean acceptable() {
        return this.finisher.apply(this.accumulator);
    }
}
