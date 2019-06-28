package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.entity.ComposableCreatureEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.util.DamageSource;
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
        return this.getOrExcept(NublarEntityComponentTypes.DINOSAUR).getDinosaur();
    }

    @Override
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        super.damageEntity(damageSrc, damageAmount);
    }
}
