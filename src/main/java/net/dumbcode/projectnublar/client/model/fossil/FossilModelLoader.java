package net.dumbcode.projectnublar.client.model.fossil;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

public enum FossilModelLoader implements IModelLoader<FossilModel> {
    INSTANCE;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public FossilModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ResourceLocation sideLocation = new ResourceLocation("");
        ResourceLocation topLocation = new ResourceLocation("");
        ResourceLocation bottomLocation = new ResourceLocation("");
        ResourceLocation particleLocation = new ResourceLocation("");
        ResourceLocation fossilLocation = new ResourceLocation("");
//        int tint = 0;
        if (modelContents.has("sideTexture")) {
            sideLocation = new ResourceLocation(modelContents.get("sideTexture").getAsString());
        }
        if (modelContents.has("topTexture")) {
            topLocation = new ResourceLocation(modelContents.get("topTexture").getAsString());
        }
        if (modelContents.has("bottomTexture")) {
            bottomLocation = new ResourceLocation(modelContents.get("bottomTexture").getAsString());
        }
        if (modelContents.has("particleTexture")) {
            particleLocation = new ResourceLocation(modelContents.get("particleTexture").getAsString());
        }
        if (modelContents.has("fossilTexture")) {
            fossilLocation = new ResourceLocation(modelContents.get("fossilTexture").getAsString());
        }
        return new FossilModel(sideLocation, topLocation, bottomLocation, particleLocation, fossilLocation);
    }
}