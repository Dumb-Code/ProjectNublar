package net.dumbcode.projectnublar.client;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.dumblibrary.server.ItemComponent;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//TODO: move this to a data generator
@Deprecated
public class DinoItemResourcePack extends ResourcePack {
    private final List<Pair<RegistryMap<Dinosaur, Item>, String>> entries = new ArrayList<>();

    private final Map<String, String> replacementCache = new HashMap<>();

    public DinoItemResourcePack() {
        super(new File("."));
        this.entries.add(Pair.of(ItemHandler.TEST_TUBES_GENETIC_MATERIAL, "test_tube_genetic_material"));
        this.entries.add(Pair.of(ItemHandler.TEST_TUBES_DNA, "test_tube_genetic_material"));
        this.entries.add(Pair.of(ItemHandler.DINOSAUR_UNINCUBATED_EGG, "unincubated_egg"));
        this.entries.add(Pair.of(ItemHandler.DINOSAUR_INCUBATED_EGG, "incubated_egg"));
    }

    @Override
    protected InputStream getResource(String location) throws IOException {
        String replacement = this.getReplacement(location);
        if(replacement != null) {
            return new ByteArrayInputStream(replacement.getBytes(StandardCharsets.UTF_8));
        }
        throw new ResourcePackFileNotFoundException(new File("."), location);
    }

    @Override
    protected boolean hasResource(String location) {
        String replacement = getReplacement(location);
        return replacement != null;
    }

    private String getReplacement(String location) {
        if(this.replacementCache.containsKey(location)) {
            return this.replacementCache.get(location);
        }
        String prefix = "assets/projectnublar/models/item/";
        String suffix = ".json";
        String result = null;
        if(location.startsWith(prefix) && location.endsWith(suffix)) {
            String name = location.substring(prefix.length(), location.length() - suffix.length());
            for (Pair<RegistryMap<Dinosaur, Item>, String> pair : this.entries) {
                for (Dinosaur dinosaur : DinosaurHandler.getRegistry().getValues()) {
                    ResourceLocation regName = dinosaur.getRegName();
                    Item item = pair.getFirst().get(dinosaur);
                    if(item.getRegistryName().getPath().equals(name)) {
                        String pairRight = pair.getSecond();
                        result =
                            "{\n" +
                            "  \"parent\": \"item/generated\",\n" +
                            "  \"textures\": {\n" +
                            "    \"layer0\": \"projectnublar:item/" + pairRight + "\",\n" +
                            "    \"layer1\": \"" + regName.getNamespace() + ":item/dinos/" + regName.getPath() + "\"\n" +
                            "  }\n" +
                            "}\n";
                    }
                }
            }
        }
        this.replacementCache.put(location, result);
        return result;
    }

    @Override
    public Collection<ResourceLocation> getResources(ResourcePackType p_225637_1_, String p_225637_2_, String p_225637_3_, int p_225637_4_, Predicate<String> p_225637_5_) {
        return Collections.emptyList(); //Invalid
    }

    @Override
    public Set<String> getNamespaces(ResourcePackType p_195759_1_) {
        return ModList.get().getMods().stream().map(ModInfo::getModId).collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return ProjectNublar.MODID + ":dino_item_override";
    }

    @Override
    public void close() {

    }

}
