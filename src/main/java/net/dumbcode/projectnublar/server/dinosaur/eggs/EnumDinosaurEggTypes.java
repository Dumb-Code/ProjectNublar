package net.dumbcode.projectnublar.server.dinosaur.eggs;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

@Getter
public enum EnumDinosaurEggTypes implements DinosaurEggType {
    TEST(12/16F, 1/4F);

    private final float eggLength;
    private final float scale;

    private final ResourceLocation texture;
    private final ResourceLocation modelLocation;
    private TabulaModel model;

    EnumDinosaurEggTypes(float length, float scale) {
        this.eggLength = length;
        this.scale = scale;
        this.texture = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/eggs/egg_type_" + this.ordinal());
        this.modelLocation = new ResourceLocation(ProjectNublar.MODID, "models/block/eggs/egg_type_" + this.ordinal());
    }


    public static void registerResourceReload() {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> {
            for (EnumDinosaurEggTypes value : values()) {
                value.model = TabulaUtils.getModel(value.modelLocation);
            }
        });
    }


}
