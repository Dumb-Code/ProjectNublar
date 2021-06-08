package net.dumbcode.projectnublar.server.entity;

import net.minecraft.util.DamageSource;

public class DamageSourceHandler {
    public static final DamageSource FENCE_ELECTRIC = new DamageSource("electric_fence").bypassArmor();
    public static final DamageSource THIRST = new DamageSource("thirst").bypassArmor().bypassMagic();
}
