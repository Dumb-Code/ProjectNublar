package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.entity.ai.EntityAttackFenceAI;

public class AttackFenceComponent extends EntityComponent implements FinalizableComponent {

    private final int priority = 1;
    private final double speed = 0.7D;

    @Override
    public void finalizeComponent(ComponentAccess entity) {

        if (entity instanceof ComposableCreatureEntity) {
            ComposableCreatureEntity creature = (ComposableCreatureEntity) entity;
            creature.goalSelector.addGoal(this.priority, new EntityAttackFenceAI(creature));
        } else {
            throw new IllegalArgumentException("Tried to attach a attack component to an ecs of class " + entity.getClass() + ". The given ecs must be a subclass of EntityCreature");
        }

    }
}
