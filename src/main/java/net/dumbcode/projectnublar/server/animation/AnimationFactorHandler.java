package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AnimationFactorHandler {

    public static final AnimationFactor<ComponentAccess> LIMB_SWING = new AnimationFactor<>("limb_swing", ComponentAccess.class, (access, type, partialTicks) -> {
        double speed = access.get(EntityComponentTypes.SPEED_TRACKING)
            .map(comp -> comp.getPreviousSpeed() + (comp.getSpeed() - comp.getPreviousSpeed()) * 0)
            .orElse(1D/12D) * 12F;
        if(type.isAngle()) {
            speed = Math.min(speed, 1.0F);
        }
        return (float)speed;
    });

    public static void register() {
        LIMB_SWING.register();
    }
}
