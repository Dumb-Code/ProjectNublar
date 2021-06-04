package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ai.SlowMoveHelper;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.sounds.SoundHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DinosaurEntity extends ComposableCreatureEntity {

    public DinosaurEntity(EntityType<? extends DinosaurEntity> type, World worldIn) {
        super(type, worldIn);

        //TODO-stream: move to a component
        this.moveHelper = new SlowMoveHelper(this);
    }

    @Override
    protected void attachComponents() {
        this.attachComponent(ComponentHandler.DINOSAUR);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /**
     * Gets the dinosaur wrapper class for this ecs.
     * @return Dinosaur class.
     */
    public Dinosaur getDinosaur() {
        return this.get(ComponentHandler.DINOSAUR).map(DinosaurComponent::getDinosaur).orElse(DinosaurHandler.TYRANNOSAURUS);
    }

    @Override
    public boolean isOnLadder() {
        return getDinosaur().getDinosaurInfomation().isCanClimb() && super.isOnLadder();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundHandler.VELOCIRAPTOR_DEATH;
    }
}
