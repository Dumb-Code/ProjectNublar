package net.dumbcode.projectnublar.client.model.fossil;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

import java.util.List;
import java.util.Map;

public class FossilBakedModel extends SimpleBakedModel {
    int tint = 0;
    public FossilBakedModel(List<BakedQuad> unculledFaces, Map<Direction, List<BakedQuad>> culledFaces, boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, TextureAtlasSprite particle, ItemCameraTransforms transforms, ItemOverrideList override, int tint) {
        super(unculledFaces, culledFaces, hasAmbientOcclusion, usesBlockLight, isGui3d, particle, transforms, override);
        this.tint = tint;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }
}
