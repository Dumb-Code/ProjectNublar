package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStoragesEvent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.projectnublar.server.entity.storage.DinosaurMultipartStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class EntityStorageOverrides {

    public static EntityComponentType.StorageOverride<MultipartEntityComponent, DinosaurMultipartStorage> DINOSAUR_MULTIPART;

    @SubscribeEvent
    public static void onRegisterStorages(RegisterStoragesEvent event) {
        DINOSAUR_MULTIPART = event.register(ComponentHandler.MULTIPART.get(), "dinosaur_multipart", DinosaurMultipartStorage::new);
    }
}
