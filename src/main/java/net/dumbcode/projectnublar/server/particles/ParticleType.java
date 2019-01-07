package net.dumbcode.projectnublar.server.particles;

import lombok.Getter;

import java.util.function.Supplier;

public enum ParticleType {
    SPARKS(() -> SparkParticle::new);
    @Getter
    private final Supplier<ParticleFactory> particleSupplier;

    ParticleType(Supplier<ParticleFactory> particleSupplier) {
        this.particleSupplier = particleSupplier;
    }
}
