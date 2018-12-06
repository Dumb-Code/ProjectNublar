package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.animation.AnimatableRenderer;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationPass;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntitySkeletalBuilderRenderer;
import net.dumbcode.projectnublar.client.render.dinosaur.EnumAnimation;
import net.dumbcode.projectnublar.client.render.dinosaur.objects.MovementAnimationPass;
import net.dumbcode.projectnublar.client.render.entity.DummyRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class DinosaurModelHandler {

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()) {
            dinosaur.setModelContainer(new ModelContainer(dinosaur.getRegName(),
                    dinosaur.getModelProperties().getModelGrowthStages(), dinosaur.getModelProperties().getMainModelMap(),
                    EnumAnimation.getNames(), dinosaur.getModelProperties().getEntityAnimatorSupplier(),
                    EnumAnimation::fromName, EnumAnimation.IDLE.get(),
                    EnumAnimation::getAnimation,
                    AnimationPass::new, MovementAnimationPass::new));
        }

        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, DinosaurRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(GyrosphereVehicle.class, DummyRenderer::new);
    }

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
    }

    private static class DinosaurRenderer extends AnimatableRenderer<DinosaurEntity> {

        DinosaurRenderer(RenderManager renderManagerIn) {
            super(renderManagerIn, entity -> entity.getDinosaur().getModelContainer(), entity -> entity.getDinosaur().getTextureLocation(entity));
        }
    }
}
