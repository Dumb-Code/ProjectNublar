package net.dumbcode.projectnublar.server.entity;

import lombok.NonNull;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

// TODO: EntityPNAnimatable would be a component instead of interface
public class DinosaurEntity extends ComposableCreatureEntity implements EntityPNAnimatable<ModelStage> {

    private static final DataParameter<Boolean> WATCHER_IS_RUNNING = EntityDataManager.createKey(DinosaurEntity.class, DataSerializers.BOOLEAN);

    private Animation<ModelStage> animation = EnumAnimation.IDLE.get();
    private int animationTick;
    private int animationLength;

    public DinosaurEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void attachComponents() {
        this.attachComponent(EntityComponentTypes.DINOSAUR);
        this.attachComponent(EntityComponentTypes.GENDER);
        this.attachComponent(EntityComponentTypes.WANDER);
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
    public Animation<ModelStage> getAnimation() {
        return this.animation;
    }

    @Override
    public void setAnimation(Animation<ModelStage> animation) {
        this.animation = animation;
        if(!this.world.isRemote) {
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation(this, animation), this.world.provider.getDimension());
        }
    }

    @Override
    public DinosaurEntitySystemInfo getInfo() {
        return this.getDinosaur().getSystemInfo();
    }

    public ModelStage getModelStage() { //TODO
        return ModelStage.ADULT;
    }

    public Dinosaur getDinosaur() {
        return this.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur;
    }


}
