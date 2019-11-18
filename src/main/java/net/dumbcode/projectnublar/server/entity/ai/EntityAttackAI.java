package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ecs.component.impl.MetabolismComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.world.World;

public class EntityAttackAI extends EntityAIAttackMelee {

    private final EntityCreature attacker;
    private final MetabolismComponent metabolism;

    private World world;

    public EntityAttackAI(EntityCreature entity, MetabolismComponent metabolism) {
        this(entity, metabolism, entity.getAIMoveSpeed());
    }

    public EntityAttackAI(EntityCreature entity, MetabolismComponent metabolism, double speed) {
        super(entity, speed, false);
        this.attacker = entity;
        this.metabolism = metabolism;
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
                if(entity.isEntityAlive() && metabolism.diet.test(entity) && entity instanceof EntityLivingBase && entity != attacker) {
                    attacker.setAttackTarget((EntityLivingBase) entity);
                    return true;
                }
            }
        }
        return false;
    }
}
