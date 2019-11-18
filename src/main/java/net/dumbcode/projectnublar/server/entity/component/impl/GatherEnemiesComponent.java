package net.dumbcode.projectnublar.server.entity.component.impl;

import net.minecraft.entity.EntityLiving;

import java.util.List;

public interface GatherEnemiesComponent {

    List<Class<? extends EntityLiving>> gatherEnemies();
}
