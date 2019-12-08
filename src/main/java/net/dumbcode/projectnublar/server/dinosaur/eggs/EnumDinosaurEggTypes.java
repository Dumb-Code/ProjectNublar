package net.dumbcode.projectnublar.server.dinosaur.eggs;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;


//TODO: remove this shit
@Getter
public enum EnumDinosaurEggTypes {
    TEST(12/16F, 1/4F);

    private final DinosaurEggType type;

    EnumDinosaurEggTypes(float length, float scale) {
        this.type = new DinosaurEggType(
            length,
            scale,
            new ResourceLocation(ProjectNublar.MODID, "textures/blocks/eggs/egg_type_" + this.ordinal()),
            new ResourceLocation(ProjectNublar.MODID, "models/block/eggs/egg_type_" + this.ordinal())
        );
    }


    public static void registerResourceReload() {
        ((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> {
            for (EnumDinosaurEggTypes value : values()) {
                value.type.clearCache();
            }
        });
    }
}
