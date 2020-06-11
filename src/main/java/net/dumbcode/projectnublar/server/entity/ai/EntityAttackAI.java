package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSound;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.sounds.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EntityAttackAI extends EntityAIAttackMelee {

//    public static final Animation ATTACK_ANIMATION = new Animation(new ResourceLocation(ProjectNublar.MODID,"attack"));

    private final EntityCreature attacker;
    private final Predicate<EntityLivingBase> enemyPredicate;
    private final AttackComponent component;

    private World world;

    public EntityAttackAI(EntityCreature entity, Predicate<EntityLivingBase> enemyPredicate, AttackComponent component) {
        super(entity, component.getSpeed(), false);
        this.attacker = entity;
        this.enemyPredicate = enemyPredicate;
        this.world = entity.world;
        this.component = component;
    }

    @Override
    public boolean shouldExecute() {
        //TODO-stream: remove this, this'll be solved with the new ai system.
        if(this.attacker instanceof ComponentAccess && ((ComponentAccess)this.attacker).get(EntityComponentTypes.SLEEPING).map(SleepingComponent::isSleeping).orElse(false)) {
            return false;
        }
        if(attacker.getAttackTarget() != null && attacker.getAttackTarget().isEntityAlive()) {
            return super.shouldExecute();
        } else {
            return findNewTarget();
        }
    }

    @Override
    public boolean isInterruptible() {
        return true;
    }

    private boolean findNewTarget() {
        if(attacker.getAttackTarget() == null || !attacker.getAttackTarget().isEntityAlive()) {
            for(Entity entity : world.loadedEntityList) {
                if(entity instanceof EntityCow) {
                    entity.isInWater();
                }
                if(entity instanceof EntityLivingBase && entity.isEntityAlive() && this.enemyPredicate.test((EntityLivingBase) entity) && entity != this.attacker) {
                    if(!(entity instanceof EntityPlayer) || (!((EntityPlayer) entity).isSpectator() && !((EntityPlayer) entity).isCreative())) {
                        attacker.setAttackTarget((EntityLivingBase) entity);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);
        if(this.attackTick == 15-7) {
            enemy.attackEntityFrom(new UnchangeableEntityDamageSource(this.attacker), this.component.getAttackDamage().getIntValue());
            if(this.attacker instanceof ComponentAccess) {
                ComponentAccess access = (ComponentAccess) this.attacker;
                access.get(EntityComponentTypes.SOUND_STORAGE).flatMap(ECSSounds.ATTACKING).ifPresent(e ->
                    this.attacker.world.playSound(null, this.attacker.posX, this.attacker.posY, this.attacker.posZ, e, SoundCategory.AMBIENT, this.attacker.getRNG().nextFloat()*0.25F+0.875F, this.attacker.getRNG().nextFloat()*0.5F+0.75F)
                );
            }
        }
        if (distToEnemySqr <= d0 && this.attackTick <= 0) {
            this.attackTick = 15;
            this.attacker.swingArm(EnumHand.MAIN_HAND);
            this.attacker.attackEntityAsMob(enemy);

            if(this.attacker.isEntityAlive() && enemy.isEntityAlive()) {
                if(this.attacker instanceof ComponentAccess) {
                    ComponentAccess access = (ComponentAccess) this.attacker;
                    access.get(EntityComponentTypes.ANIMATION).ifPresent(component ->
                        component.playAnimation(access, this.attacker.getRNG().nextFloat() < 0.2 ? AnimationHandler.POUNCE.createEntry() : AnimationHandler.ATTACK.createEntry().withSpeed(1.2F), AttackComponent.ATTACK_CHANNEL)
                    );
                }
            }

        }
    }

    private static class UnchangeableEntityDamageSource extends EntityDamageSource {

        public UnchangeableEntityDamageSource(@Nullable Entity damageSourceEntityIn) {
            super("mob", damageSourceEntityIn);
        }

        @Override
        public boolean isDifficultyScaled() {
            return false;
        }
    }
}
