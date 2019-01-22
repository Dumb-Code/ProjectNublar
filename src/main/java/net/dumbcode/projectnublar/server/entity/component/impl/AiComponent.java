package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;

public interface AiComponent extends EntityComponent {
    void apply(EntityAITasks tasks, Entity entity);
}
