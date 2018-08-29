package net.dumbcode.projectnublar.server.entity.component;

import javax.annotation.Nullable;
import java.util.HashMap;

public class EntityComponentMap extends HashMap<EntityComponentType<?>, EntityComponent>{
    public <T extends EntityComponent> void put(EntityComponentType<T> type, T component) {
        super.put(type, component);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T get(EntityComponentType<T> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return (T) component;
        }
        return null;
    }
}
