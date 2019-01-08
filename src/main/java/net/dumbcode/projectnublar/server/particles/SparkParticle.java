package net.dumbcode.projectnublar.server.particles;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class SparkParticle extends Particle {
    public SparkParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int[] ints) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        Random rand = worldIn.rand;
        this.motionX = xSpeedIn * 0.05 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.motionY = ySpeedIn * 0.05 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.motionZ = zSpeedIn * 0.05 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.particleGravity = 0.75F;
        this.particleAge /= 4;
        this.particleTextureIndexX = rand.nextInt(16);
        this.particleTextureIndexY = 0;
        this.particleScale = 0.25F;
    }
    
    @Override
    public void move(double x, double y, double z) {
        BlockElectricFence.collidable = false;
        super.move(x, y, z);
        BlockElectricFence.collidable = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.particleTextureIndexX++;

    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/particles/map.png"));
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
        Tessellator.getInstance().draw();
    }

    @Override
    public int getFXLayer() {
        return 3;
    }
}
