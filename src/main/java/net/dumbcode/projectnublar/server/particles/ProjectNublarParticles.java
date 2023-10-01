package net.dumbcode.projectnublar.server.particles;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class ProjectNublarParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ProjectNublar.MODID);

    public static final RegistryObject<BasicParticleType> SPARK = REGISTER.register("spark", () -> new BasicParticleType(false));

}
