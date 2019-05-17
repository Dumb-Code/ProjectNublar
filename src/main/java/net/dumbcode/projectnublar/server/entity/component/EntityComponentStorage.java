package net.dumbcode.projectnublar.server.entity.component;

import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;

public interface EntityComponentStorage<T extends EntityComponent> {

    T construct();

    default void readJson(JsonObject json) {

    }

    default void writeJson(JsonObject json){

    }

}
