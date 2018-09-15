package net.dumbcode.projectnublar.server.entity;

import lombok.NonNull;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

// TODO: EntityPNAnimatable would be a component instead of interface
public class DinosaurEntity extends ComposableCreatureEntity implements EntityPNAnimatable {

    private static final DataParameter<Boolean> WATCHER_IS_RUNNING = EntityDataManager.createKey(DinosaurEntity.class, DataSerializers.BOOLEAN);

    private Animation animation = EnumAnimation.IDLE.get();
    private int animationTick;
    private int animationLength;

    public int modelIndex; //Not final name

    public DinosaurEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void attachComponents() {
        this.attachComponent(EntityComponentTypes.DINOSAUR);
        this.attachComponent(EntityComponentTypes.GENDER);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WATCHER_IS_RUNNING, false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            this.dataManager.set(WATCHER_IS_RUNNING, this.getAIMoveSpeed() > this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        }
    }

    @Override
    public boolean isRunning() {
        return this.dataManager.get(WATCHER_IS_RUNNING);
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

            Dinosaur dinosaur = this.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur;
            this.animationLength = (int) dinosaur.getModelContainer().getPoseHandler().getAnimationLength(this.animation, this.getGrowthStage());

            AnimationHandler.INSTANCE.sendAnimationMessage(this, newAnimation);
        }
    }

    public GrowthStage getGrowthStage() { //TODO
        return GrowthStage.ADULT;
    }

    public Dinosaur getDinosaur() {
        return this.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.world.isRemote) {
            modelIndex++;
        }
        player.swingArm(hand);
        return true;
    }

    @Override
    public Animation[] getAnimations() {
        return EnumAnimation.getAnimations();
    }
}
