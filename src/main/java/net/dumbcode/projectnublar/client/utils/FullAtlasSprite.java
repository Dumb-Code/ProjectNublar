package net.dumbcode.projectnublar.client.utils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class FullAtlasSprite extends TextureAtlasSprite {

    public FullAtlasSprite(ResourceLocation location) {
        super(location.toString());
        this.initSprite(this.width = 1, this.height = 1, 0, 0, false);
    }
}
