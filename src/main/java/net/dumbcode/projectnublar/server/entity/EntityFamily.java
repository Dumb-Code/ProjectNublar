package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;

import java.lang.reflect.Array;

public class EntityFamily<E> {
    private final ComponentAccess[] matchedEntities;

    public EntityFamily(ComponentAccess[] matchedEntities) {
        this.matchedEntities = matchedEntities;
    }

    @SuppressWarnings("unchecked")
    public E[] getEntities() {
        return (E[]) this.matchedEntities;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T[] populateBuffer(EntityComponentType<T> type, T[] buffer) {
        ComponentAccess[] matched = this.matchedEntities;
        if (buffer.length != matched.length) {
            buffer = (T[]) Array.newInstance(type.getType(), matched.length);
        }
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = matched[i].getOrNull(type);
        }
        return buffer;
    }
}
