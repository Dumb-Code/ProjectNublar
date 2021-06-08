package net.dumbcode.projectnublar.server.sounds;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundHandler {

    public static DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ProjectNublar.MODID);

    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_BITE = create("velociraptor_bite");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_CALL = create("velociraptor_call");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_CHARGE_BREATH = create("velociraptor_charge_breath");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_DEATH = create("velociraptor_death");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_EXHALE = create("velociraptor_exhale");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_GROWL = create("velociraptor_growl");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_HISS = create("velociraptor_hiss");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_HURT = create("velociraptor_hurt");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_LAUGH = create("velociraptor_laugh");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_SCREECH = create("velociraptor_screech");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_SNORE = create("velociraptor_snore");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_SNORT = create("velociraptor_snort");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_FLESH_CRUNCH = create("velociraptor_flesh_crunch");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_FLESH_RIP = create("velociraptor_flesh_rip");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_DRINK = create("velociraptor_drink");
    public static final RegistryObject<SoundEvent> VELOCIRAPTOR_IDLE = create("velociraptor_idle");

    private static RegistryObject<SoundEvent> create(String name) {
        return REGISTER.register(name, () -> new SoundEvent(new ResourceLocation(ProjectNublar.MODID, name)));
    }

}
