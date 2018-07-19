package net.dumbcode.projectnublar.server.entity;

import lombok.NonNull;
import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurAnimations;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class DinosaurEntity extends EntityCreature implements IEntityAdditionalSpawnData, IAnimatedEntity {

    private Dinosaur dinosaur = Dinosaur.MISSING;

    private Animation animation = DinosaurAnimations.IDLE.get();
    private int animationTick;
    private int animationLength;

    public DinosaurEntity(World worldIn, Dinosaur dinosaur) {
        this(worldIn);
        this.dinosaur = dinosaur;
    }

    public DinosaurEntity(World worldIn) {
        super(worldIn);
    }

    private void setDinosaur(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }

    public Dinosaur getDinosaur() {
        return dinosaur;
    }

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        this.animationTick = tick;
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }

    @Override
    public void setAnimation(@NonNull Animation newAnimation) {
        Animation oldAnimation = this.animation;

        this.animation = newAnimation;

        if (oldAnimation != newAnimation) {
            this.animationTick = 0;
            this.animationLength = (int) this.dinosaur.getPoseHandler().getAnimationLength(this.animation, this.getGrowthStage());

            AnimationHandler.INSTANCE.sendAnimationMessage(this, newAnimation);
        }
    }

    private GrowthStage getGrowthStage() {
        return GrowthStage.ADULT;
    }

    @Override
    public Animation[] getAnimations() {
        return DinosaurAnimations.getAnimations();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeRegistryEntry(buffer, this.dinosaur);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.dinosaur = ByteBufUtils.readRegistryEntry(buffer, ProjectNublar.DINOSAUR_REGISTRY);
    }
}
