package net.dumbcode.projectnublar.client.render.dinosaur;

import com.google.gson.Gson;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaModelContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@UtilityClass
public class TabulaUtils {

    public static TabulaModel getModel(ResourceLocation location){
        return new TabulaModel(getModelContainer(location));
    }

    public static TabulaModelContainer getModelContainer(ResourceLocation location){
        if(!location.getResourcePath().endsWith(".tbl")) {
            location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".tbl");
        }
        try {
            @Cleanup InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            return new Gson().fromJson(new InputStreamReader(getModelJsonStream(stream)), TabulaModelContainer.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load model " + location, e);
        }
    }

    private static InputStream getModelJsonStream(InputStream file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file);
        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals("model.json")) {
                return zip;
            }
        }

        throw new IOException("No model.json present");
    }
}
