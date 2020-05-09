package net.dumbcode.projectnublar.server.entity.component.impl;

import net.minecraft.entity.EntityLivingBase;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface GatherEnemiesComponent {
    void gatherEnemyPredicates(Consumer<Predicate<EntityLivingBase>> registry);
}
