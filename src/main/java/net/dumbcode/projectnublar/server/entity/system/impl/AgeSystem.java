package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Iterator;

public class AgeSystem implements EntitySystem {

    private AgeComponent[] ages = new AgeComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.AGE);
        this.ages = family.populateBuffer(ComponentHandler.AGE);
        this.entities = family.getEntities();
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.ages.length; i++) {
            this.update(this.ages[i], this.entities[i]);
        }
    }

    private void update(AgeComponent age, Entity entity) {
        AgeStage start = age.stage;
        int ageoff = age.getAgeInTicks();

        Iterator<AgeStage> iterator = age.getOrderedAges().iterator();
        while(ageoff > 0 && iterator.hasNext()) {
            age.stage = iterator.next();
            ageoff -= age.stage.getTime();
            if(age.stage.getTime() == -1) {
                break;
            }
        }


        if(age.stage == null) {
            age.setPercentageStage(0F);
        } else {
            age.setPercentageStage((ageoff + age.stage.getTime()) / (float) age.stage.getTime());
        }

        if(start != age.stage) {
            if(entity instanceof ComponentAccess) {
                ((ComponentAccess) entity).finalizeComponents();
            }
        }

        age.setAgeInTicks(age.getAgeInTicks() + 1);
    }

    @SubscribeEvent
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        ClientWorld world = Minecraft.getInstance().level;
        if(world != null && !Minecraft.getInstance().isPaused()) {
            for (Entity entity : world.entitiesForRendering()) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(ComponentHandler.AGE).ifPresent(a -> this.update(a, entity));
                }
            }
        }
    }
}
