package net.dumbcode.projectnublar.server.fossil.base;

import lombok.Getter;

public enum FossilTier {
    WEATHERED(0, 40, 0.3),
    FRAGMENTED(0.05f, 20, 0.3),
    COMMON(0.1f, 15, 0.3),
    UNEARTHED(0.2f, 10, 0.3),
    ANCIENT(0.3f, 5, 0.6),
    WELL_PRESERVED(0.4f, 4, 0.6),
    EXQUISITE(0.5f, 3, 0.6),
    RARE(0.7f, 1.5f, 0.6),
    PRIMAL(0.9f, 1, 0.6),
    TIMELESS(1, 0.5f, 1);

    /**
     * float from 0-1
     * A percentage divided by 100 so that it is normalized to between 0 and 1
     */
    private final float DNAGatherChance;
    /**
     * Must add up to 100. Essentially percent spawn rate
     */
    private final float weight;
    @Getter
    private final double dnaValue;

    FossilTier(float DNAGatherChance, float weight, double dnaValue) {
        this.DNAGatherChance = DNAGatherChance;
        this.weight = weight;
        this.dnaValue = dnaValue;
    }

    public static FossilTier randomTier() {
        double totalWeight = 0.0;
        for (FossilTier tier : values()) {
            totalWeight += tier.weight;
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < values().length - 1; ++idx) {
            r -= values()[idx].weight;
            if (r <= 0.0) break;
        }
        return values()[idx];
    }
}
