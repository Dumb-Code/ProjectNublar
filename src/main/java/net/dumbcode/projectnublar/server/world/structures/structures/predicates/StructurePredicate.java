package net.dumbcode.projectnublar.server.world.structures.structures.predicates;

import net.dumbcode.projectnublar.server.world.structures.StructureInstance;

import java.util.function.Consumer;

public interface StructurePredicate {
    default boolean canBuildDirect(StructureInstance instance) {
        return true;
    }

    default void setupTraversers(Consumer<PredicateTraverser> registry) {
        //To be optional overridden
    }
}
