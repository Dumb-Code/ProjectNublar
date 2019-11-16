package net.dumbcode.projectnublar.server.dinosaur.data;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

public enum DinosaurPeriod {
    QUATERNARY("quaternary", 2.588F, 0.0F),
    NEOGENE("neogene", 23.03F, 2.589F),
    PALEOGENE("paleogene", 66.0F, 23.04F),
    CRETACEOUS("cretaceous", 145.5F, 66.1F),
    JURASSIC("jurassic", 201.3F, 145.6F),
    TRIASSIC("triassic", 252.17F, 201.4F),
    PERMIAN("permian", 298.9F, 252.18F),
    CARBONIFEROUS("carboniferous", 358.9F, 299.0F),
    DEVONIAN("devonian", 419.2F, 359.0F),
    SILURIAN("silurian", 443.4F, 419.3F),
    ORDOVICIAN("ordovician", 485.4F, 443.5F),
    CAMBRIAN("cambrian", 541.0F, 485.5F);

    public static final float yStart = 64;

    private final String name;
    private final float startTime;
    private final float endTime;

    DinosaurPeriod(String name, float startTime, float endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getYLevel(Random random) {
        int min = MathHelper.floor(DinosaurPeriod.yStart - DinosaurPeriod.yStart * startTime / 541F);
        int max = MathHelper.floor(DinosaurPeriod.yStart - DinosaurPeriod.yStart * endTime / 541F);
        return random.nextInt(max - min) + min;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
