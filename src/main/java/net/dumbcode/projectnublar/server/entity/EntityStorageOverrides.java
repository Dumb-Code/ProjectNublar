package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterStorages;
import net.dumbcode.dumblibrary.server.ecs.component.impl.CloseProximityAngryComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.dumbcode.projectnublar.server.entity.storage.CloseProximityBlacklistStorage;
import net.dumbcode.projectnublar.server.entity.storage.DinosaurMultipartStorage;

public class EntityStorageOverrides {

    public static EntityComponentType.StorageOverride<MultipartEntityComponent, DinosaurMultipartStorage> DINOSAUR_MULTIPART;
    public static EntityComponentType.StorageOverride<CloseProximityAngryComponent, CloseProximityBlacklistStorage> CLOSE_PROXIMITY_BLACKLIST;

    public static void onRegisterStorages() {
        DINOSAUR_MULTIPART = EntityComponentType.registerStorageOverride(ComponentHandler.MULTIPART.get(), "dinosaur_multipart", DinosaurMultipartStorage::new);
        CLOSE_PROXIMITY_BLACKLIST = EntityComponentType.registerStorageOverride(EntityComponentTypes.CLOSE_PROXIMITY_ANGRY.get(), "close_proximity_blacklist", CloseProximityBlacklistStorage::new);
    }
}
