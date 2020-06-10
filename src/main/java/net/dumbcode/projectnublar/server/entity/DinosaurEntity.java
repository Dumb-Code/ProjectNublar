package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ai.SlowMoveHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.world.World;

public class DinosaurEntity extends ComposableCreatureEntity {

    public DinosaurEntity(World worldIn) {
        super(worldIn);

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
        return this.getOrExcept(ComponentHandler.DINOSAUR).getDinosaur();
    }

    @Override
    public boolean isOnLadder() {
        return getDinosaur().getDinosaurInfomation().isCanClimb() && super.isOnLadder();
    }
}
