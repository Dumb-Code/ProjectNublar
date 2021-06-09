package net.dumbcode.projectnublar.server.plants;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.dumblibrary.server.registry.EarlyDeferredRegister;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class PlantHandler {

    public static final EarlyDeferredRegister<Plant> REGISTER = EarlyDeferredRegister.create(Plant.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<Plant>> PLANT_REGISTRY = REGISTER.makeRegistry("plants", RegistryBuilder::new);


    public static final RegistryObject<Plant> CYCAD = REGISTER.register("cycad", Cycad::new);
    public static final RegistryObject<Plant> SERENNA_VERIFORMANS = REGISTER.register("serenna_veriformans", SerennaVeriformans::new);

    public static IForgeRegistry<Plant> getRegistry() {
        return PLANT_REGISTRY.get();
    }
}
