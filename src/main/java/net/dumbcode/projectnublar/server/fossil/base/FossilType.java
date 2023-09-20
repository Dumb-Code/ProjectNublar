package net.dumbcode.projectnublar.server.fossil.base;

import java.util.Random;

public enum FossilType {
    PETRIFIED(0.8F),
    MOLD_CAST(0.6F),
    CARBON_FILM(0.7F),
    TRACE(0.5F),
    PRESERVED_REMAINS(0.9F),
    COMPRESSION(0.4F),
    IMPRESSION(0.3F),
    PSEUDO(0.2F);

    private final float DNAMultiplier;

    FossilType(float DNAMultiplier) {
        this.DNAMultiplier = DNAMultiplier;
    }

    public static FossilType randomType() {
        return values()[new Random().nextInt(values().length)];
    }
}
