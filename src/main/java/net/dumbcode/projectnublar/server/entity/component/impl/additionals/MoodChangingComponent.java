package net.dumbcode.projectnublar.server.entity.component.impl.additionals;

import net.dumbcode.projectnublar.server.entity.mood.MoodReason;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface MoodChangingComponent {
    void applyMoods(BiConsumer<MoodReason, Supplier<Float>> acceptor);

    default boolean isDirty() {
        return true;
    }
}
