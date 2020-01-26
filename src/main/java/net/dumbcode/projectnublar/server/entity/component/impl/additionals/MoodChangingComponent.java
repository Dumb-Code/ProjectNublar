package net.dumbcode.projectnublar.server.entity.component.impl.additionals;

import net.dumbcode.projectnublar.server.entity.mood.MoodReason;

import java.util.function.BiConsumer;

public interface MoodChangingComponent {
    void applyMoods(BiConsumer<MoodReason, Number> acceptor);
}
