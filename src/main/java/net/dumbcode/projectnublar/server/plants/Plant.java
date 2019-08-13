package net.dumbcode.projectnublar.server.plants;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockstateComponentProperty;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Plant extends IForgeRegistryEntry.Impl<Plant> {
    protected final EntityComponentAttacher baseAttacher = new EntityComponentAttacher();
    protected final Map<String, EntityComponentAttacher> stateOverrides = new HashMap<>();


    public BlockstateComponentProperty createOverrideProperty() {
        return new BlockstateComponentProperty(this.baseAttacher, this.stateOverrides);
    }

    public void attachComponents() {
    }

    public void attachOverride(String overrideName, Consumer<EntityComponentAttacher> onAttach) {
        EntityComponentAttacher attacher = new EntityComponentAttacher();
        onAttach.accept(attacher);
        this.stateOverrides.put(overrideName, attacher);
    }


}
