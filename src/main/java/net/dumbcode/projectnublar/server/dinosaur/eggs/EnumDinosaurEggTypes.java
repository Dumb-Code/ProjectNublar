package net.dumbcode.projectnublar.server.dinosaur.eggs;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;


@Getter
public enum EnumDinosaurEggTypes { //TODO: check scale
    NORMAL(10F/16F, 1/4F),
    ROUND(6.5F/16F, 1/4F),
    TALL_1(14F/16F, 1/4F),
    TALL_2(13.5F/16F, 1/4F),
    TYRANNOSAURUS(12.5F/16F, 1/4F),
    VELOCIRAPTOR(10F/16F, 1/4F);

    private final DinosaurEggType type;
    private final DinosaurEggType qrType;

    EnumDinosaurEggTypes(float length, float scale) {
        ResourceLocation modelLocation = new ResourceLocation(ProjectNublar.MODID, "models/entities/eggs/egg_" + this.name());
        ResourceLocation mainTextureLocation = new ResourceLocation(ProjectNublar.MODID, "textures/entities/eggs/egg_" + this.name() + ".png");
        this.type = new DinosaurEggType(length, scale, modelLocation, mainTextureLocation);
        this.qrType = new DinosaurEggType(length, scale, modelLocation, mainTextureLocation, new ResourceLocation(ProjectNublar.MODID, "textures/entities/eggs/egg_" + this.name() + "_qr.png"));
    }


    public static void registerResourceReload() {
        ((IReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener((stage, resourceManager, profiler, profiler1, executor, executor1) -> CompletableFuture.runAsync(() -> {
            for (EnumDinosaurEggTypes value : values()) {
                value.type.clearCache();
            }
        }));
    }
}
