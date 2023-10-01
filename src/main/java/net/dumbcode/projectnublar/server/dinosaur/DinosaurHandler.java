package net.dumbcode.projectnublar.server.dinosaur;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class DinosaurHandler {

    public static final EarlyDeferredRegister<Dinosaur> REGISTER = EarlyDeferredRegister.create(Dinosaur.class, ProjectNublar.MODID);

    private static final Supplier<IForgeRegistry<Dinosaur>> DINOSAUR_REGISTRY = REGISTER.makeRegistry("dinosaurs", () ->
        new RegistryBuilder<Dinosaur>()
            .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "tyrannosaurus"))
            .set((key, isNetwork) -> DinosaurHandler.TYRANNOSAURUS.get())
    );

    public static final RegistryObject<Dinosaur> TYRANNOSAURUS = REGISTER.register("tyrannosaurus", Tyrannosaurus::new);
    public static final RegistryObject<Dinosaur> DILOPHOSAURUS = REGISTER.register("dilophosaurus", Dilophosaurus::new);
    public static final RegistryObject<Dinosaur> VELOCIRAPTOR_JP = REGISTER.register("velociraptor_jp", VelociraptorJP::new);
    public static final RegistryObject<Dinosaur> VELOCIRAPTOR_JP3 = REGISTER.register("velociraptor_jp3", VelociraptorJP::new);

    public static IForgeRegistry<Dinosaur> getRegistry() {
        return DINOSAUR_REGISTRY.get();
    }

}
