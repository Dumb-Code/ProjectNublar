package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GoalManagerComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class GoalSystem implements EntitySystem {

    private GoalManagerComponent[] goals = new GoalManagerComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.AGE);
        this.goals = family.populateBuffer(ComponentHandler.GOAL);
    }

    @Override
    public void update(World world) {
        for (GoalManagerComponent goal : this.goals) {
            goal.goalManager.tick();
        }
    }

}
