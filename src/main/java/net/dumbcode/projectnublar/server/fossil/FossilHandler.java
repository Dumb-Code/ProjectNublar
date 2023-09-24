package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FossilHandler {
    public static final PostEarlyDeferredRegister<Fossil> REGISTER = PostEarlyDeferredRegister.create(Fossil.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<Fossil>> FOSSIL_REGISTRY = REGISTER.makeRegistry("fossils", RegistryBuilder::new);

    public static RegistryObject<Fossil> AMMONITE = REGISTER.register("ammonite", () -> new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/ammonite"), "Ammonite", true, null, null, null));
    public static RegistryObject<Fossil> TRILOBITE = REGISTER.register("trilobite", () -> new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/trilobite"), "Trilobite", true, null, null, null));

    public static RegistryMap<Dinosaur, Fossil> FEATHERS = createSimpleFossilMap("feather", "block/feather");
    public static RegistryMap<Dinosaur, Fossil> FEET = createSimpleFossilMap("feet", "block/feet");
    public static RegistryMap<Dinosaur, Fossil> TOOTH = createSimpleFossilMap("tooth", "block/tooth");

    private static RegistryMap<Dinosaur, Fossil> createSimpleFossilMap(String fossilName, String texture) {
        return createMap("%s_" + fossilName, dinosaur ->
                new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, texture),
                        dinosaur.getFormattedName() + " Feather", true, () -> dinosaur, "feather", null));
    }
    private static RegistryMap<Dinosaur, Fossil> createMap(String format, Function<Dinosaur, Fossil> supplier) {
        RegistryMap<Dinosaur, Fossil> map = new RegistryMap<>();
        REGISTER.beforeRegister(() -> {
            for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
                map.putRegistry(dinosaur, REGISTER.register(
                        String.format(format, dinosaur.getFormattedName()),
                        () -> supplier.apply(dinosaur)
                ));
            }
        });
        return map;
    }

}
