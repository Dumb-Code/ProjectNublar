package net.dumbcode.projectnublar.server.utils;

import com.google.gson.GsonBuilder;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.SkeletalInformation;

public interface JsonHandlers {

    static GsonBuilder registerAllHandlers(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeHierarchyAdapter(Dinosaur.class, new Dinosaur.JsonAdapter());
        gsonBuilder.registerTypeHierarchyAdapter(SkeletalInformation.class, new SkeletalInformation.JsonAdapter());
        gsonBuilder.registerTypeHierarchyAdapter(ModelProperties.class, new ModelProperties.JsonAdapter());
        gsonBuilder.registerTypeHierarchyAdapter(ItemProperties.class, new ItemProperties.JsonAdapter());
        return gsonBuilder;
    }
}
