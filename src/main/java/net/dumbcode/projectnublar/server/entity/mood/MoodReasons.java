package net.dumbcode.projectnublar.server.entity.mood;

public class MoodReasons {

    public static MoodReason HYDRATED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, 1F)
        .translationKey("projectnublar.mood.reason.hydrated")
        .build();
    public static MoodReason DEHYDRATED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, -1F)
        .moodType(MoodTypes.HAPPY, -0.25F)
        .translationKey("projectnublar.mood.reason.dehydrated")
        .build();

    public static MoodReason FULL = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, 1F)
        .moodType(MoodTypes.HAPPY, 1.25F)
        .translationKey("projectnublar.mood.reason.full")
        .build();

    public static MoodReason STARVED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, -1F)
        .moodType(MoodTypes.HAPPY, -0.25F)
        .translationKey("projectnublar.mood.reason.hungry")
        .build();
}
