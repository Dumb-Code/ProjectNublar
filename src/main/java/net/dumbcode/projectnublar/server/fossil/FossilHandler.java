package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class FossilHandler {
    public static final PostEarlyDeferredRegister<Fossil> REGISTER = PostEarlyDeferredRegister.create(Fossil.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<Fossil>> FOSSIL_REGISTRY = REGISTER.makeRegistry("fossils", RegistryBuilder::new);

    public static RegistryObject<Fossil> AMMONITE = REGISTER.register("ammonite", () -> new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/ammonite"), "Ammonite", true, null, null, null));
    public static RegistryObject<Fossil> TRILOBITE = REGISTER.register("trilobite", (new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/trilobite"), "Trilobite", true, null, null, null));



}
