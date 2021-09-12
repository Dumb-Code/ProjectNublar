package net.dumbcode.projectnublar.server.data;

import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

public class NameChangingFinishedRecipe implements IFinishedRecipe {

    private final IFinishedRecipe delegate;
    private final ResourceLocation location;

    public NameChangingFinishedRecipe(IFinishedRecipe delegate, String modid) {
        this.delegate = delegate;
        this.location = new ResourceLocation(modid, delegate.getId().getPath());
    }

    @Override
    public ResourceLocation getId() {
        return this.location;
    }

    @Override
    public void serializeRecipeData(JsonObject object) {
        this.delegate.serializeRecipeData(object);
    }

    @Override
    public JsonObject serializeRecipe() {
        return this.delegate.serializeRecipe();
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return this.delegate.getType();
    }

    @Override
    public JsonObject serializeAdvancement() {
        return this.delegate.serializeAdvancement();
    }

    @Override
    public ResourceLocation getAdvancementId() {
        return this.delegate.getAdvancementId();
    }
}
