package net.dumbcode.projectnublar.client.model.fossil;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class FossilModel implements IModelGeometry<FossilModel> {
    ResourceLocation sideLocation;
    ResourceLocation topLocation;
    ResourceLocation bottomLocation;
    ResourceLocation particleLocation;
    ResourceLocation fossilLocation;

    public FossilModel(ResourceLocation sideLocation, ResourceLocation topLocation, ResourceLocation bottomLocation, ResourceLocation particleLocation, ResourceLocation fossilLocation) {
        this.sideLocation = sideLocation;
        this.topLocation = topLocation;
        this.bottomLocation = bottomLocation;
        this.particleLocation = particleLocation;
        this.fossilLocation = fossilLocation;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new FossilBakedModel(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation, sideLocation, topLocation, bottomLocation, particleLocation, fossilLocation);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return new ArrayList<>();
    }
}
