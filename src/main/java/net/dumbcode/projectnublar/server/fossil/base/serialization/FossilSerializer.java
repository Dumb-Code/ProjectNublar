package net.dumbcode.projectnublar.server.fossil.base.serialization;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.fossil.Fossils;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class FossilSerializer {
    public static byte[] serializeModel(UnSerializedFossilModel object) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        gson.toJson(unserializedModelToJson(object), writer);
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public static byte[] serializeBlockstate(String id) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        JsonObject object = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject none = new JsonObject();
        none.addProperty("model", id);
        variants.add("", none);
        object.add("variants", variants);
        gson.toJson(object, writer);
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public static JsonObject unserializedModelToJson(UnSerializedFossilModel src) {
        JsonObject object = new JsonObject();
        object.addProperty("parent", "projectnublar:block/fossil");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", src.particleTexture);
        textures.addProperty("bottom", src.bottomTexture);
        textures.addProperty("top", src.topTexture);
        textures.addProperty("side", src.sideTexture);
        textures.addProperty("overlay", src.fossilTexture);
        object.add("textures", textures);
        return object;
    }

    public static byte[] generatePackMcmeta() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

        JsonObject object = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("description", "Auto-Generated Fossil pack for ProjectNublar");
        pack.addProperty("pack_format", 6);
        object.add("pack", pack);
        gson.toJson(object, writer);

        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }


    public static void serializeMineableTag(Multimap<StoneType, Fossil> fossils) {
        ImmutableMultimap.Builder<String, Pair<StoneType, Collection<Fossil>>> tagsBuilder = new ImmutableMultimap.Builder<>();
        for (StoneType type : fossils.keys()) {
            tagsBuilder.put(type.mineableBy, new Pair<>(type, fossils.get(type)));
        }
        Multimap<String, Pair<StoneType, Collection<Fossil>>> tags = tagsBuilder.build();
        for (String key : tags.keys()) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

            JsonObject object = new JsonObject();
            object.addProperty("replace", false);
            JsonArray values = new JsonArray();
            for (Pair<StoneType, Collection<Fossil>> type : tags.get(key)) {
                for (Fossil fossil : type.getSecond()) {
                    values.add(type.getFirst().name.replace(" ", "_").toLowerCase() + "_" + fossil.name.replace(" ", "_").toLowerCase());
                }
            }
            object.add("values", values);
            gson.toJson(object, writer);

            try {
                writer.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            Fossils.PACK.addData(new ResourceLocation("tags/blocks/" + key + ".json"), stream.toByteArray());
        }
    }
}
