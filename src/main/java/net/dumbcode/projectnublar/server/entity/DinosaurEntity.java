package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.entity.ComposableCreatureEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.minecraft.world.World;

public class DinosaurEntity extends ComposableCreatureEntity {

    public DinosaurEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void attachComponents() {
        this.attachComponent(NublarEntityComponentTypes.DINOSAUR);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    /**
     * Gets the dinosaur wrapper class for this entity.
     * @return Dinosaur class.
     */
    public Dinosaur getDinosaur() {
        return this.getOrExcept(NublarEntityComponentTypes.DINOSAUR).dinosaur;
    }

    /**
     * Gets the current age of the entity.
     * @return Dinosaur age.
     */
    public ModelStage getState() {
        return this.get(NublarEntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(ModelStage.ADULT);
    }

    /**
     * Gets the current model scale of the dinosaur by
     * using it's model stages and state.
     * @return model scale.
     */
    public double getCurrentScale() {
        return getDinosaur().getScale().get(getState());
    }
}
