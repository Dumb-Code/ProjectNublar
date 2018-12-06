package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.entity.ai.EntityAITasks;

public abstract class AiComponent implements EntityComponent {
    public abstract void apply(EntityAITasks tasks);
}
