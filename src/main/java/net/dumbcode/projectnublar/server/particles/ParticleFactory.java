package net.dumbcode.projectnublar.server.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ParticleFactory {
    @SideOnly(Side.CLIENT)
    Particle createParticle(World world, double xPos, double yPos, double zPos, double xMotion, double yMotion, double zMotion, int... ints);
}
