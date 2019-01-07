package net.dumbcode.projectnublar.server.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class SparkParticle extends Particle {
    public SparkParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int[] ints) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.multiplyVelocity(0.2F);
        this.motionY *= 0.1F;
    }

    @Override
    public void onUpdate() {
//        this.motionX = 0;
//        this.motionY = 0;
//        this.motionZ = 0;
        super.onUpdate();
    }

    @Override
    public int getFXLayer() {
        return 0;
    }
}
