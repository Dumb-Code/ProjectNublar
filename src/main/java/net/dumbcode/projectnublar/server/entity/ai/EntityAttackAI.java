package net.dumbcode.projectnublar.server.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EntityAttackAI extends EntityAIAttackMelee {

    private final EntityCreature attacker;
    private final Predicate<EntityLivingBase> enemyPredicate;

    private World world;

    public EntityAttackAI(EntityCreature entity, Predicate<EntityLivingBase> enemyPredicate) {
        this(entity, enemyPredicate, entity.getAIMoveSpeed());
    }

    public EntityAttackAI(EntityCreature entity, Predicate<EntityLivingBase> enemyPredicate, double speed) {
        super(entity, speed, false);
        this.attacker = entity;
        this.enemyPredicate = enemyPredicate;
        this.world = entity.world;
    }

    @Override
    public boolean shouldExecute() {
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
                if(entity instanceof EntityLivingBase && entity.isEntityAlive() && this.enemyPredicate.test((EntityLivingBase) entity) && entity != this.attacker) {
                    attacker.setAttackTarget((EntityLivingBase) entity);
                    return true;
                }
            }
        }
        return false;
    }
}
