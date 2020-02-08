package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DamageSourceHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MetabolismSystem implements EntitySystem {

    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.METABOLISM);
        this.metabolism = family.populateBuffer(ComponentHandler.METABOLISM, this.metabolism);
        this.entities = family.getEntities();
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.metabolism.length; i++) {
            if(this.entities[i].ticksExisted % 20 == 0) {
                MetabolismComponent meta = this.metabolism[i];
                meta.setFood((float) MathHelper.clamp(meta.getFood() - meta.getFoodRate().getValue(), 0, meta.getMaxFood().getValue()));
                meta.setWater((float) MathHelper.clamp(meta.getWater() - meta.getWaterRate().getValue(), 0, meta.getMaxWater().getValue()));

                if(this.entities[i] instanceof EntityLivingBase) {
                    if(meta.getFood() == 0) {
                        this.entities[i].attackEntityFrom(DamageSource.STARVE, 1F);
                    }
                    if(meta.getWater() == 0) {
                        this.entities[i].attackEntityFrom(DamageSourceHandler.THIRST, 1F);
                    }
                }
            }
        }
    }


}
