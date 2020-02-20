package net.dumbcode.projectnublar.server.entity.mood;

public class MoodReasons {

    public static MoodReason HYDRATED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, a -> a)
        .translationKey("projectnublar.mood.reason.hydrated")
        .build();
    public static MoodReason DEHYDRATED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, a -> -a)
        .moodType(MoodTypes.HAPPY, a -> -a / 4F)
        .translationKey("projectnublar.mood.reason.dehydrated")
        .build();

    public static MoodReason FULL = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, a -> a)
        .moodType(MoodTypes.HAPPY, a -> a * 1.25F)
        .moodType(MoodTypes.AWAKE, a -> Math.max(4*a - 7, 0)) //https://www.desmos.com/calculator/jz2rn3fdsh to make sure this only happens when very full up
        .translationKey("projectnublar.mood.reason.full")
        .build();

    public static MoodReason STARVED = MoodReason.builder()
        .moodType(MoodTypes.STRESSFUL, a -> -a)
        .moodType(MoodTypes.HAPPY, a -> -a / 4F)
        .translationKey("projectnublar.mood.reason.hungry")
        .build();
}
