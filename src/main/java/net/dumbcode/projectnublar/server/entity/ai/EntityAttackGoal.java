package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;

public class EntityAttackGoal extends EntityGoal {

    private final CreatureEntity attacker;
    private final Predicate<LivingEntity> enemyPredicate;
    private final AttackComponent component;
    private final AnimationComponent animationComponent;

    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;

    public EntityAttackGoal(GoalManager manager, CreatureEntity attacker, Predicate<LivingEntity> enemyPredicate, AttackComponent component, AnimationComponent animationComponent) {
        super(manager);
        this.attacker = attacker;
        this.enemyPredicate = enemyPredicate;
        this.component = component;
        this.animationComponent = animationComponent;
    }

    @Override
    protected void tick() {
        if(this.attacker.getTarget() == null || !this.attacker.getTarget().isAlive()) {
            this.finish();
            return;
        }

        LivingEntity enemy = this.attacker.getTarget();
        this.attacker.getLookControl().setLookAt(enemy, 30.0F, 30.0F);
        double distance = this.attacker.distanceToSqr(enemy.getX(), enemy.getY(), enemy.getZ());
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if(this.ticksUntilNextPathRecalculation <= 0) {
            this.ticksUntilNextPathRecalculation = 4 + RANDOM.nextInt(7);

            if (distance > 1024.0D) {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (distance > 256.0D) {
                this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.attacker.getNavigation().moveTo(enemy, this.component.getSpeed())) {
                this.ticksUntilNextPathRecalculation += 15;
            }
        }


        double attackDistance = Math.max(this.attacker.getBoundingBoxForCulling().getXsize(), this.attacker.getBoundingBoxForCulling().getZsize()) / 2F + enemy.getBbWidth();

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        boolean shouldAttack = this.animationComponent != null ? !this.animationComponent.isChannelActive(AttackComponent.ATTACK_CHANNEL) : this.ticksUntilNextAttack <= 0;

        if(distance <= attackDistance*attackDistance && shouldAttack) {
            enemy.hurt(new UnchangeableEntityDamageSource(this.attacker), (float) this.component.getAttackDamage().getValue());
            if(this.attacker.isAlive() && enemy.isAlive()) {
                boolean pounce = this.attacker.getRandom().nextFloat() < 0.3;
                if(this.animationComponent != null) {
                    this.animationComponent.playAnimation(pounce ? AnimationHandler.POUNCE : AnimationHandler.ATTACK, AttackComponent.ATTACK_CHANNEL).withSpeed(pounce ? 1F: 1.2F);
                }
                if(pounce) {
                    this.attacker.setDeltaMovement(this.attacker.getDeltaMovement().add(0, 0.3, 0));
                }
            }
        }
    }

    @Override
    public boolean onStarted() {
        if(this.attacker.getTarget() == null || !this.attacker.getTarget().isAlive()) {
            List<Entity> entities = this.attacker.level.getEntities(this.attacker, this.attacker.getBoundingBox().inflate(50, 20, 50), EntityPredicates.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof LivingEntity && this.enemyPredicate.test((LivingEntity) entity)));
            if(!entities.isEmpty()) {
                this.attacker.setTarget((LivingEntity) entities.get(0));
                return true;
            }
        }
        return this.attacker.getTarget() != null && this.attacker.getTarget().isAlive();
    }

    @Override
    protected OptionalDouble getImportance() {
        return OptionalDouble.of(30);
    }

    private static class UnchangeableEntityDamageSource extends EntityDamageSource {

        public UnchangeableEntityDamageSource(Entity damageSourceEntityIn) {
            super("mob", damageSourceEntityIn);
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }
    }
}
