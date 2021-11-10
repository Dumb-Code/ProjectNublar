package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.MovePredicateComponent;
import net.dumbcode.projectnublar.server.entity.ai.FloatAi;
import net.dumbcode.projectnublar.server.entity.ai.WanderAI;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class WanderComponent extends EntityComponent implements FinalizableComponent {

    //TODO-stream: ew -- ai should be on a system that is task based, only executing one at a time, with tasks having "importance"
    //This would allow for entities to wake up from sleep as they're too hungry
    private Supplier<Boolean> canExecute = () -> true;

    private boolean avoidWater = true;

    private int priority = 5;
    private double speed = 0.4D;
    private int chance = 50;

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof CreatureEntity) {
            CreatureEntity creature = (CreatureEntity) entity;
            creature.goalSelector.addGoal(this.priority, new WanderAI(creature, this));
            creature.goalSelector.addGoal(this.priority - 1, new FloatAi(creature));
//            ((CreatureEntity) entity).tasks.addTask(this.priority, this.avoidWater ? new EntityAIWanderAvoidWater(creature, this.speed, 1F / this.chance) : new EntityAIWander(creature, this.speed, this.chance));
        } else {
            throw new IllegalArgumentException("Tried to attach a wander component to an ecs of class " + entity.getClass() + ". The given ecs must be a subclass of MobEntity");
        }

        List<Supplier<Boolean>> registry = new ArrayList<>();
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof MovePredicateComponent) {
                ((MovePredicateComponent) component).addBlockers(registry::add);
            }
        }
        this.canExecute = registry.stream().reduce(() -> true, (b1, b2) -> () -> b1.get() && b2.get());

    }
}
