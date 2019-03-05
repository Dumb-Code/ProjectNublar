package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.world.World;

//todo: dummyify IEntityMultiPart, as to remove it from this entity
public class DinosaurEntity extends ComposableCreatureEntity {

    public DinosaurEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void attachComponents() {
        this.attachComponent(EntityComponentTypes.DINOSAUR);
        this.attachComponent(EntityComponentTypes.GENDER);
        this.attachComponent(EntityComponentTypes.AGE);
        this.attachComponent(EntityComponentTypes.HERD);
        this.attachComponent(EntityComponentTypes.WANDER_AI);
        this.attachComponent(EntityComponentTypes.ANIMATION);
        this.attachComponent(EntityComponentTypes.METABOLISM);
        this.attachComponent(EntityComponentTypes.MULTIPART);
    }

    //Helper method
    public Dinosaur getDinosaur() {
        return this.getOrExcept(EntityComponentTypes.DINOSAUR).getDinosaur();
    }


}
