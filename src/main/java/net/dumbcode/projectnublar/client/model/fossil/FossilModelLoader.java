package net.dumbcode.projectnublar.client.model.fossil;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.obj.LineReader;
import net.minecraftforge.client.model.obj.OBJModel;

public enum FossilModelLoader implements IModelLoader<FossilModel> {
	INSTANCE;

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
	}

	@Override
	public FossilModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
		ResourceLocation stoneLocation = new ResourceLocation("");
		ResourceLocation fossilLocation = new ResourceLocation("");
		ResourceLocation particle = new ResourceLocation("");
		int tint = 0;
		if (modelContents.has("stone")) {
			stoneLocation = new ResourceLocation(modelContents.get("stone").getAsString());
		}
		if (modelContents.has("fossil")) {
			fossilLocation = new ResourceLocation(modelContents.get("fossil").getAsString());
		}
		if (modelContents.has("tint")) {
			tint = modelContents.get("tint").getAsInt();
		}
		if (modelContents.has("particle")) {
			particle = new ResourceLocation(modelContents.get("particle").getAsString());
		}
		ResourceLocation modeLocation = new ResourceLocation(ProjectNublar.MODID, "models/block/fossil.obj");
		try(LineReader reader = new LineReader(Minecraft.getInstance().getResourceManager().getResource(modeLocation))) {
			return new FossilModel(reader, new OBJModel.ModelSettings(modeLocation, false, true, false, false, null), stoneLocation, fossilLocation, tint, particle);
		} catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}