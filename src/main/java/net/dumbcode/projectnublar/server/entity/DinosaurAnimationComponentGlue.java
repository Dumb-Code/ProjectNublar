package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.events.FeedingChangeEvent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class DinosaurAnimationComponentGlue {

    public static final int METABOLISM_CHANNEL = 61;

    @SubscribeEvent
    public static void onFoodChangeEvent(FeedingChangeEvent event) {
        event.getComponentAccess().get(EntityComponentTypes.ANIMATION).ifPresent(a -> {
            if(event.isStarted()) {
                a.playAnimation(event.getComponentAccess(), new AnimationLayer.AnimationEntry(AnimationHandler.EATING), METABOLISM_CHANNEL);
            } else {
                a.stopAnimation(METABOLISM_CHANNEL);
            }
        });
    }
}
