package net.dumbcode.projectnublar.server.fossil.base;

public enum FossilTier {
    WEATHERED(0, 40),
    FRAGMENTED(0.05f, 20),
    COMMON(0.1f, 15),
    UNEARTHED(0.2f, 10),
    ANCIENT(0.3f, 5),
    WELL_PRESERVED(0.4f, 4),
    EXQUISITE(0.5f, 3),
    RARE(0.7f, 1.5f),
    PRIMAL(0.9f, 1),
    TIMELESS(1, 0.5f);

    /**
     * float from 0-1
     * A percentage divided by 100 so that it is normalized to between 0 and 1
     */
    private final float DNAGatherChance;
    /**
     * Must add up to 100. Essentially percent spawn rate
     */
    private final float weight;

    FossilTier(float DNAGatherChance, float weight) {
        this.DNAGatherChance = DNAGatherChance;
        this.weight = weight;
    }
}
