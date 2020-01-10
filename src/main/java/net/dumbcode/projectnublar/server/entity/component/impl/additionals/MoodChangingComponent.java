package net.dumbcode.projectnublar.server.entity.component.impl.additionals;

import java.util.function.BiConsumer;

public interface MoodChangingComponent {
    void applyMoodModifiers(BiConsumer<Integer, String> acceptor);
}
