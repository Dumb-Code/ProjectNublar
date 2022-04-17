package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class  EntityAttackAI extends MeleeAttackGoal {

//    public static final Animation ATTACK_ANIMATION = new Animation(new ResourceLocation(ProjectNublar.MODID,"attack"));

    private final CreatureEntity attacker;
    private final Predicate<LivingEntity> enemyPredicate;
    private final AttackComponent component;

    private World world;
    private int attackTick;

    public EntityAttackAI(CreatureEntity entity, Predicate<LivingEntity> enemyPredicate, AttackComponent component) {
        super(entity, component.getSpeed(), false);
        this.attacker = entity;
        this.enemyPredicate = enemyPredicate;
        this.world = entity.level;
        this.component = component;
    }

    @Override
    public boolean canUse() {
        //TODO-stream: remove this, this'll be solved with the new ai system.
        if(this.attacker instanceof ComponentAccess && ((ComponentAccess)this.attacker).get(EntityComponentTypes.SLEEPING).map(SleepingComponent::isSleeping).orElse(false)) {
            return false;
        }
        if(attacker.getTarget() != null && attacker.getTarget().isAlive()) {
            return super.canUse();
        } else {
            return findNewTarget();
        }
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    private boolean findNewTarget() {
        if(attacker.getTarget() == null || !attacker.getTarget().isAlive()) {
            List<Entity> entities = world.getEntities(attacker, attacker.getBoundingBox().inflate(50, 20, 50), EntityPredicates.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof LivingEntity && this.enemyPredicate.test((LivingEntity) entity)));
            if(!entities.isEmpty()) {
                attacker.setTarget((LivingEntity) entities.get(0));
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        super.start();
        if(this.attacker instanceof ComponentAccess) {
            ComponentAccess access = (ComponentAccess) this.attacker;
            access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.CALLING).ifPresent(e -> {
                Vector3d pos = this.attacker.position();
                this.attacker.level.playSound(null, pos.x, pos.y, pos.z, e, SoundCategory.AMBIENT, this.attacker.getRandom().nextFloat()*0.25F+1.25F, this.attacker.getRandom().nextFloat()*0.5F+0.75F);
            });
            access.get(EntityComponentTypes.ANIMATION).ifPresent(component ->
                component.playAnimation(AnimationHandler.CALL_SHORT, AttackComponent.ATTACK_CHANNEL)
            );
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if(this.attackTick == 15-7) {
            enemy.hurt(new UnchangeableEntityDamageSource(this.attacker), this.component.getAttackDamage().getIntValue());
            if(this.attacker instanceof ComponentAccess) {
                ComponentAccess access = (ComponentAccess) this.attacker;
                Vector3d pos = this.attacker.position();
                access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.ATTACKING).ifPresent(e ->
                    this.attacker.level.playSound(null, pos.x, pos.y, pos.z, e, SoundCategory.AMBIENT, this.attacker.getRandom().nextFloat()*0.25F+0.875F, this.attacker.getRandom().nextFloat()*0.5F+0.75F)
                );
            }
        }
        if (distToEnemySqr <= d0 && this.attackTick <= 0) {
            this.attackTick = 15;
            this.attacker.swing(Hand.MAIN_HAND);
            this.attacker.doHurtTarget(enemy);

            if(this.attacker.isAlive() && enemy.isAlive()) {
                if(this.attacker instanceof ComponentAccess) {
                    ComponentAccess access = (ComponentAccess) this.attacker;
                    boolean b = this.attacker.getRandom().nextFloat() < 0.3;
                    access.get(EntityComponentTypes.ANIMATION).ifPresent(component ->
                        component.playAnimation(b ? AnimationHandler.POUNCE : AnimationHandler.ATTACK, AttackComponent.ATTACK_CHANNEL).withSpeed(b ? 1F: 1.2F)
                    );
                    if(b) {
                        this.attacker.setDeltaMovement(this.attacker.getDeltaMovement().add(0, 0.3, 0));
                    }
                }
            }
        }
        this.attackTick--;
    }

    private static class UnchangeableEntityDamageSource extends EntityDamageSource {

        public UnchangeableEntityDamageSource(@Nullable Entity damageSourceEntityIn) {
            super("mob", damageSourceEntityIn);
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }
    }
}
