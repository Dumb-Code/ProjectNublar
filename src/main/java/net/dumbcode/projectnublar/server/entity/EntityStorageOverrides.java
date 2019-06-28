package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.entity.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.entity.component.RegisterStoragesEvent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.projectnublar.server.entity.storage.DinosaurMultipartStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class EntityStorageOverrides {
    //todo: move storage overrides to registry event ?

    public static EntityComponentType.StorageOverride<MultipartEntityComponent, DinosaurMultipartStorage> DINOSAUR_STORAGE;

    @SubscribeEvent
    public static void onRegisterStorages(RegisterStoragesEvent event) {

        DINOSAUR_STORAGE = event.register(ComponentHandler.MULTIPART, "dinosaur_age", DinosaurMultipartStorage::new);

    }

}
