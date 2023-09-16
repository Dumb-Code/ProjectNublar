package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.tags.JTag;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FossilSerializer {
    public static byte[] serialize(UnSerializedFossilModel object) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        gson.toJson(serialize(object, true), writer);
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public static byte[] serialize(String id) {
        System.out.println("w");
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
        System.out.println("m");
        return stream.toByteArray();
    }

    public static byte[] serialize(ResourceLocation id) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        JsonObject object = new JsonObject();
        object.addProperty("parent", "projectnublar:block/" + id.getPath());
        gson.toJson(object, writer);
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public static JsonObject serialize(UnSerializedFossilModel src, boolean a) {
        System.out.println("q");
        JsonObject object = new JsonObject();
        object.addProperty("loader", ProjectNublar.MODID + ":fossil");
        object.addProperty("parent", "block/cube");
        JsonObject particle = new JsonObject();
        particle.addProperty("particle", src.stoneTexture);
        object.add("textures", particle);
        object.addProperty("stone", src.stoneTexture);
        object.addProperty("fossil", src.fossilTexture);
        object.addProperty("tint", src.tint);
        System.out.println("b");
        return object;
    }

    public static byte[] serialize() {
        System.out.println("n");
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
        System.out.println("p");
        return stream.toByteArray();
    }


    public static void serialize(Multimap<StoneType, Fossil> fossils) {
        System.out.println("rgesdg");
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
        System.out.println("wrtwgg");
    }
}
