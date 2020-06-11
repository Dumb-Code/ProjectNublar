package net.dumbcode.projectnublar.server.sounds;

import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class SoundHandler {

    public static final SoundEvent VELOCIRAPTOR_BITE = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_CALL = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_CHARGE_BREATH = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_DEATH = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_EXHALE = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_GROWL = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_HISS = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_HURT = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_LAUGH = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_SCREECH = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_SNORE = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_SNORT = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_FLESH_CRUNCH = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_FLESH_RIP = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_DRINK = InjectedUtils.injected();
    public static final SoundEvent VELOCIRAPTOR_IDLE = InjectedUtils.injected();

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        registerAll(event.getRegistry(),
            "velociraptor_bite",  "velociraptor_call", "velociraptor_charge_breath", "velociraptor_death",
            "velociraptor_exhale", "velociraptor_growl", "velociraptor_hiss", "velociraptor_hurt", "velociraptor_laugh",
            "velociraptor_screech", "velociraptor_snore", "velociraptor_snort", "velociraptor_flesh_crunch",
            "velociraptor_flesh_rip", "velociraptor_drink", "velociraptor_idle"
        );
    }

    private static void registerAll(IForgeRegistry<SoundEvent> registry, String... args) {
        for (String arg : args) {
            ResourceLocation location = new ResourceLocation(ProjectNublar.MODID, arg);
            registry.register(new SoundEvent(location).setRegistryName(location));
        }
    }

}
