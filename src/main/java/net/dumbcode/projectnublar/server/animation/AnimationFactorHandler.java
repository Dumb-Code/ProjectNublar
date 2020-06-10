package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;

public class AnimationFactorHandler {

    public static final AnimationFactor<ComponentAccess> LIMB_SWING = new AnimationFactor<>("limb_swing", ComponentAccess.class, (access, type, partialTicks) -> {
        double speed = access.get(EntityComponentTypes.SPEED_TRACKING)
            .map(comp -> comp.getPreviousSpeed() + (comp.getSpeed() - comp.getPreviousSpeed()) * partialTicks)
            .orElse(1D/15D) * 15F;
        if(type.isAngle()) {
            speed = Math.min(speed, 1.0F);
        }
        return (float)speed;
    });

    public static void register() {
        LIMB_SWING.register();
    }
}
