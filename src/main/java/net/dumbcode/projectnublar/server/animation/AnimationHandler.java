package net.dumbcode.projectnublar.server.animation;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class AnimationHandler {

    public static final Animation WALKING = InjectedUtils.injected();
    public static final Animation ATTACK = InjectedUtils.injected();
    public static final Animation CALL_SHORT = InjectedUtils.injected();
    public static final Animation CALLING = InjectedUtils.injected();
    public static final Animation DRINKING = InjectedUtils.injected();
    public static final Animation EATING = InjectedUtils.injected();
    public static final Animation INJURED = InjectedUtils.injected();
    public static final Animation LOOK_AROUND = InjectedUtils.injected();
    public static final Animation RESTING = InjectedUtils.injected();
    public static final Animation ROARING = InjectedUtils.injected();
    public static final Animation RUNNING = InjectedUtils.injected();
    public static final Animation SLEEPING = InjectedUtils.injected();
    public static final Animation SNIFF_AIR = InjectedUtils.injected();
    public static final Animation SNIFF_GROUND = InjectedUtils.injected();

    @SubscribeEvent
    public static void onAnimationRegister(RegistryEvent.Register<Animation> event) {
        event.getRegistry().registerAll(
                new Animation().setRegistryName("walking"),
                new Animation().setRegistryName("attack"),
                new Animation().setRegistryName("call_short"),
                new Animation().setRegistryName("calling"),
                new Animation().setRegistryName("drinking"),
                new Animation().setRegistryName("eating"),
                new Animation().setRegistryName("injured"),
                new Animation().setRegistryName("look_around"),
                new Animation().setRegistryName("resting"),
                new Animation().setRegistryName("roaring"),
                new Animation().setRegistryName("running"),
                new Animation().setRegistryName("sleeping"),
                new Animation().setRegistryName("sniff_air"),
                new Animation().setRegistryName("sniff_ground")

        );
    }
}
