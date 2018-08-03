package net.dumbcode.projectnublar.server.dinosaur.data;

import com.google.gson.*;
import lombok.Data;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
public class EntityProperties {
    private Function<World, DinosaurEntity> entityCreateFunction = DinosaurEntity::new;

    public void copyFrom(EntityProperties other) {

    }

    public static class JsonAdapter implements JsonSerializer<EntityProperties>, JsonDeserializer<EntityProperties> {

        @Override
        public EntityProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            EntityProperties result = new EntityProperties();
            return result;
        }

        @Override
        public JsonElement serialize(EntityProperties src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            return object;
        }
    }
}
