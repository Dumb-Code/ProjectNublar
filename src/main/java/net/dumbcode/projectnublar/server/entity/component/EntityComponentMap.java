package net.dumbcode.projectnublar.server.entity.component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EntityComponentMap {
    private final Map<EntityComponentType<?>, EntityComponent> backing = new HashMap<>();

    public <T extends EntityComponent> void put(EntityComponentType<T> type, T component) {
        this.backing.put(type, component);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T get(EntityComponentType<T> type) {
        EntityComponent component = this.backing.get(type);
        if (component != null) {
            return (T) component;
        }
        return null;
    }
}
