package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class EntityHandler {

    public static final EntityEntry DINOSAUR = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(
            EntityEntryBuilder.create()
                .entity(DinosaurEntity.class)
                .factory(DinosaurEntity::new)
                .name(ProjectNublar.MODID + ".dinosaur")
                .tracker(128, 3, true)
                .id(new ResourceLocation(ProjectNublar.MODID, "dinosaur"), 0)
                .build(),
            EntityEntryBuilder.create()
                .entity(DinosaurEggEntity.class)
                .factory(DinosaurEggEntity::new)
                .name(ProjectNublar.MODID + ".dinosaur_egg")
                .tracker(32, 10, true)
                .id(new ResourceLocation(ProjectNublar.MODID, "dinosaur_egg"), 3)
                .build(),

            EntityEntryBuilder.create()
                .entity(GyrosphereVehicle.class)
                .factory(GyrosphereVehicle::new)
                .name(ProjectNublar.MODID + ".gyrosphere")
                .tracker(64, 10, true)
                .id(new ResourceLocation(ProjectNublar.MODID, "gyrosphere"), 1)
                .build(),

            EntityEntryBuilder.create()
                .entity(EntityPart.class)
                .factory(EntityPart::new)
                .name(ProjectNublar.MODID + ".dummypart")
                .tracker(128, 20, true)
                .id(new ResourceLocation(ProjectNublar.MODID, "dummypart"), 2)
                .build()
        );
    }
}
