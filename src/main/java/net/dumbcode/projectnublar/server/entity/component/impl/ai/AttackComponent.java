package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.EntityGoalSupplier;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherEnemiesComponent;
import net.dumbcode.projectnublar.server.entity.ai.EntityAttackGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class AttackComponent extends EntityComponent implements EntityGoalSupplier {

    public static final int ATTACK_CHANNEL = 62;

    private final int priority = 1;
    private final double speed = 1D;
    private final ModifiableField attackDamage = new ModifiableField();

    @Override
    public void addGoals(GoalManager manager, Consumer<EntityGoal> consumer, ComponentAccess access) {
        //TODO: add this as a storage:
        this.attackDamage.setBaseValue(6);


        List<Predicate<LivingEntity>> enemyPredicates = new ArrayList<>();
        for (EntityComponent component : access.getAllComponents()) {
            if(component instanceof GatherEnemiesComponent) {
                ((GatherEnemiesComponent) component).gatherEnemyPredicates(enemyPredicates::add);
            }
        }

        Predicate<LivingEntity> predicate = enemyPredicates.stream().reduce(Predicate::or).orElse(e -> false);
        if(access instanceof CreatureEntity) {
            consumer.accept(new EntityAttackGoal(manager, (CreatureEntity) access, predicate, this, access.getOrNull(EntityComponentTypes.ANIMATION)));
        }
    }
}
