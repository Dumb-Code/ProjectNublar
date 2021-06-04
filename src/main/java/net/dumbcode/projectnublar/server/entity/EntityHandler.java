package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityHandler {

    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, ProjectNublar.MODID);

    public static final RegistryObject<EntityType<DinosaurEntity>> DINOSAUR = REGISTER.register("dinosaur", () ->
        EntityType.Builder.of(DinosaurEntity::new, EntityClassification.CREATURE)
            .clientTrackingRange(128)
            .setTrackingRange(128)
            .setUpdateInterval(3)
            .setShouldReceiveVelocityUpdates(true)
            .build("dinosaur")
    );

    public static final RegistryObject<EntityType<DinosaurEggEntity>> DINOSAUR_EGG = REGISTER.register("dinosaur_egg", () ->
        EntityType.Builder.<DinosaurEggEntity>of(DinosaurEggEntity::new, EntityClassification.AMBIENT)
            .clientTrackingRange(32)
            .setTrackingRange(32)
            .setUpdateInterval(10)
            .setShouldReceiveVelocityUpdates(true)
            .build("dinosaur_egg")
    );

    public static final RegistryObject<EntityType<GyrosphereVehicle>> GYROSPHERE = REGISTER.register("gyrosphere", () ->
        EntityType.Builder.<GyrosphereVehicle>of(GyrosphereVehicle::new, EntityClassification.AMBIENT)
            .clientTrackingRange(64)
            .setTrackingRange(64)
            .setUpdateInterval(10)
            .setShouldReceiveVelocityUpdates(true)
            .sized(4, 4)
            .build("gyrosphere")
    );

    public static final RegistryObject<EntityType<EntityPart>> DUMMY_PART = REGISTER.register("gyrosphere", () ->
        EntityType.Builder.<GyrosphereVehicle>of(EntityPart::new, EntityClassification.AMBIENT)
            .clientTrackingRange(128)
            .setTrackingRange(128)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build("gyrosphere")
    );
}
