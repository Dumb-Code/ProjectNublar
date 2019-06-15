package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.entity.ComposableCreatureEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
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

    //Helper method
    public Dinosaur getDinosaur() {
        return this.getOrExcept(NublarEntityComponentTypes.DINOSAUR).dinosaur;
    }

    //Helper method
    public ModelStage getState() {
        return this.get(NublarEntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(ModelStage.ADULT);
    }

}
