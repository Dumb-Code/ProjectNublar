package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.entity.ai.EntityAttackAI;
import net.dumbcode.projectnublar.server.entity.component.impl.GatherEnemiesComponent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AttackComponent extends EntityComponent implements FinalizableComponent {

    private final int priority = 1;
    private final double speed = 0.7D;

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        List<Predicate<EntityLivingBase>> enemyPredicates = new ArrayList<>();

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof GatherEnemiesComponent) {
                ((GatherEnemiesComponent) component).gatherEnemyPredicates(enemyPredicates::add);
            }
        }
        if(entity instanceof ComposableCreatureEntity) {
            enemyPredicates.stream().reduce(Predicate::and).ifPresent(predicate -> {
                ComposableCreatureEntity creature = (ComposableCreatureEntity) entity;
                creature.tasks.addTask(this.priority, new EntityAttackAI(creature, predicate, speed));
            });
        } else {
            throw new IllegalArgumentException("Tried to attach a attack component to an ecs of class " + entity.getClass() + ". The given ecs must be a subclass of EntityCreature");
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {

    }
}
