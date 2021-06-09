package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DefenseSystem implements EntitySystem {

    @SubscribeEvent
    public void onEntityDamage(LivingDamageEvent event) {
        if(event.getEntity() instanceof ComponentAccess) {
            ((ComponentAccess) event.getEntity())
                .get(ComponentHandler.DEFENSE)
                .ifPresent(c -> event.setAmount((float) Math.max(event.getAmount() - c.getDefense().getValue(), 1D)));
        }
    }
}
