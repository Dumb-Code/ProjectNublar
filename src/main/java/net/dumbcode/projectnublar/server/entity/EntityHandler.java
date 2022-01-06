package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
        EntityType.Builder.<DinosaurEggEntity>of(DinosaurEggEntity::new, EntityClassification.MISC)
            .clientTrackingRange(32)
            .setTrackingRange(32)
            .setUpdateInterval(10)
            .setShouldReceiveVelocityUpdates(true)
            .build("dinosaur_egg")
    );

    public static final RegistryObject<EntityType<GyrosphereVehicle>> GYROSPHERE = REGISTER.register("gyrosphere", () ->
        EntityType.Builder.<GyrosphereVehicle>of(GyrosphereVehicle::new, EntityClassification.MISC)
            .clientTrackingRange(64)
            .setTrackingRange(64)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(false)
            .sized(4, 4)
            .build("gyrosphere")
    );

    public static final RegistryObject<EntityType<EntityPart>> DUMMY_PART = REGISTER.register("entity_part", () ->
        EntityType.Builder.<EntityPart>of(EntityPart::new, EntityClassification.MISC)
            .clientTrackingRange(128)
            .setTrackingRange(128)
            .setUpdateInterval(20)
            .setShouldReceiveVelocityUpdates(true)
            .build("entity_part")
    );

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(DINOSAUR.get(), DinosaurEntity.createMobAttributes().build());
    }
}
