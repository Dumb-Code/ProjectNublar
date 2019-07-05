package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationFactor;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class AnimationFactorHandler {

    private AnimationFactorHandler() {}

    public static final AnimationFactor LIMB_SWING = InjectedUtils.injected();

    @SubscribeEvent
    public static void onAnimationFactorRegister(RegistryEvent.Register<AnimationFactor> event) {
        event.getRegistry().registerAll(
                new AnimationFactor((access, type, partialTicks) -> {
                    Entity entity = (Entity) access;
                    double speed = access.get(EntityComponentTypes.SPEED_TRACKING)
                            .map(comp -> comp.getPreviousSpeed() + (comp.getSpeed() - comp.getPreviousSpeed()) * partialTicks)
                            .orElse(Math.sqrt(entity.motionX*entity.motionX + entity.motionZ*entity.motionZ)) * 12F;
                    if(type.isAngle()) {
                        speed = Math.min(speed, 1.0F);
                    }
                    return (float)speed;
                }).setRegistryName("limb_swing")
        );
    }
}
