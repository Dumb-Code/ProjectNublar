package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.minecraft.entity.Entity;

import java.lang.reflect.Array;

public class EntityFamily {
    private final Entity[] matchedEntities;

    public EntityFamily(Entity[] matchedEntities) {
        this.matchedEntities = matchedEntities;
    }

    public Entity[] getEntities() {
        return this.matchedEntities;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T[] populateBuffer(EntityComponentType<T> type, T[] buffer) {
        Entity[] matched = this.matchedEntities;
        if (buffer == null || buffer.length != matched.length) {
            buffer = (T[]) Array.newInstance(type.getType(), matched.length);
        }
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ((ComponentAccess) matched[i]).getOrNull(type);
        }
        return buffer;
    }
}
