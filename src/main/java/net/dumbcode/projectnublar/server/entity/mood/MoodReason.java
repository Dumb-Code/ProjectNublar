package net.dumbcode.projectnublar.server.entity.mood;

import lombok.*;

import java.util.Map;
import java.util.function.UnaryOperator;

@Getter
@Builder
@RequiredArgsConstructor
public class MoodReason {
    @Singular
    private final Map<MoodType, UnaryOperator<Float>> moodTypes;
    @NonNull private final String translationKey;
}
