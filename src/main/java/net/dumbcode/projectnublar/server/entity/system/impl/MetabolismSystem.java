package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DamageSourceHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MetabolismSystem implements EntitySystem {

    private static final int BLOCKS_PER_SECOND_FOOD = 5;
    private static final int BLOCKS_PER_SECOND_WATER = 3;
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
            Entity entity = this.entities[i];
            if(entity.tickCount % 20 == 0) {
                MetabolismComponent meta = this.metabolism[i];
                if (meta.getPreviouslyCheckedPosition() == null) {
                    meta.setPreviouslyCheckedPosition(entity.position());
                }
                double x = Math.abs(meta.getPreviouslyCheckedPosition().x - entity.position().x);
                double y = Math.abs(meta.getPreviouslyCheckedPosition().y - entity.position().y);
                double z = Math.abs(meta.getPreviouslyCheckedPosition().z - entity.position().z);
                meta.setPreviouslyCheckedPosition(entity.position());
                double dist = Math.sqrt(x*x + y*y + z*z);
                double foodPunish = dist * BLOCKS_PER_SECOND_FOOD;
                double waterPunish = dist * BLOCKS_PER_SECOND_WATER;

                meta.setFood((float) MathHelper.clamp(meta.getFood() - meta.getFoodRate().getValue() - foodPunish, 0, meta.getMaxFood().getValue()));
                meta.setWater((float) MathHelper.clamp(meta.getWater() - meta.getWaterRate().getValue() - waterPunish, 0, meta.getMaxWater().getValue()));

                //As food gets smaller, this gets smaller
                //At 25%, linearly decrease to 0
                //https://www.desmos.com/calculator/zklfbe6wnn
                double foodEnergyModifier = Math.min(4 * meta.getFood() / meta.getMaxFood().getValue(), 1);
                meta.setEnergy((float) (meta.getEnergy() + meta.getEnergyRate().getValue() * foodEnergyModifier));

                if(entity instanceof LivingEntity) {
                    if(meta.getFood() == 0) {
                        entity.hurt(DamageSource.STARVE, 1F);
                    }
                    if(meta.getWater() == 0) {
                        entity.hurt(DamageSourceHandler.THIRST, 1F);
                    }
                }
            }
        }
    }


}
