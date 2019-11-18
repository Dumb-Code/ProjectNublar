package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.world.World;

import java.util.List;

public class EntityAttackAI extends EntityAIAttackMelee {

    private final EntityCreature attacker;
    private final List<Class<? extends EntityLiving>> enemies;

    private World world;

    public EntityAttackAI(EntityCreature entity, List<Class<? extends EntityLiving>> enemies) {
        this(entity, enemies, entity.getAIMoveSpeed());
    }

    public EntityAttackAI(EntityCreature entity, List<Class<? extends EntityLiving>> enemies, double speed) {
        super(entity, speed, false);
        this.attacker = entity;
        this.enemies = enemies;
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
                if(entity.isEntityAlive() && enemies.contains(entity.getClass()) && entity != attacker) {
                    attacker.setAttackTarget((EntityLivingBase) entity);
                    return true;
                }
            }
        }
        return false;
    }
}
