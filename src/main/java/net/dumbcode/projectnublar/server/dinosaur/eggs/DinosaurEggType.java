package net.dumbcode.projectnublar.server.dinosaur.eggs;

import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public interface DinosaurEggType {
    float getScale();
    float getEggLength();
    ModelBase getModel();
    ResourceLocation getTexture();

    DinosaurEggType EMPTY = new DinosaurEggType() {
        @Override
        public float getScale() {
            return 0;
        }

        @Override
        public float getEggLength() {
            return 0;
        }

        @Override
        public ModelBase getModel() {
            return ModelMissing.INSTANCE;
        }

        @Override
        public ResourceLocation getTexture() {
            return TextureMap.LOCATION_MISSING_TEXTURE;
        }
    };
}
