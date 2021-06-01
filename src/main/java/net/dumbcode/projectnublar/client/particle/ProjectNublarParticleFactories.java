package net.dumbcode.projectnublar.client.particle;

import net.dumbcode.projectnublar.server.particles.ProjectNublarParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;

public class ProjectNublarParticleFactories {

    public static void onParticleFactoriesRegister(ParticleFactoryRegisterEvent event) {
        ParticleManager manager = Minecraft.getInstance().particleEngine;
        manager.register(ProjectNublarParticles.SPARK.get(), SparkParticle.Factory::new);
    }

}
