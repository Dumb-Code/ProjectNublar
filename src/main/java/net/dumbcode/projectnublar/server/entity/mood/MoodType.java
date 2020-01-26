package net.dumbcode.projectnublar.server.entity.mood;

import lombok.Data;

@Data
public class MoodType {
    private final String positiveTranslationKey;
    private final String negativeTranslationKey;

    public MoodType(String translationKey) {
        this.positiveTranslationKey = translationKey + ".positive";
        this.negativeTranslationKey = translationKey + ".negative";
    }
}
