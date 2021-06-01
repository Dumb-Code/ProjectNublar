package net.dumbcode.projectnublar.client.particle;

import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

import javax.annotation.Nullable;
import java.util.Random;

public class SparkParticle extends SpriteTexturedParticle {

    private final IAnimatedSprite sprite;

    public SparkParticle(ClientWorld worldIn, double x, double y, double z, double mx, double my, double mz, IAnimatedSprite sprite) {
        super(worldIn, x, y, z, mx, my, mz);
        Random rand = worldIn.random;
        this.sprite = sprite;
        this.xd = mx * 0.075 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.yd = my * 0.075 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.zd = mz * 0.075 + (rand.nextFloat() * 2 - 1) * 0.02;
        this.gravity = 0.75F;
        this.age /= 4;
        this.scale(0.25F);
        this.setSpriteFromAge(this.sprite);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprite);
    }

    @Override
    public void move(double x, double y, double z) {
        BlockConnectableBase.setCollidableClient(false);
        super.move(x, y, z);
        BlockConnectableBase.setCollidableClient(true);
    }

    @RequiredArgsConstructor
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;

        @Nullable
        @Override
        public Particle createParticle(BasicParticleType data, ClientWorld world, double x, double y, double z, double mx, double my, double mz) {
            return new SparkParticle(world, x, y, z, mx, my, mz, this.sprite);
        }
    }
}
