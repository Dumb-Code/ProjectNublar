package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.animation.AnimatableRenderer;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFencePoleRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFenceRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntitySkeletalBuilderRenderer;
import net.dumbcode.projectnublar.client.render.entity.GyrosphereRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class DinosaurModelHandler {

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()) {
            dinosaur.setModelContainer(new ModelContainer<>(dinosaur.getRegName(), new DinosaurEntitySystemInfo(dinosaur)));
        }

        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, DinosaurRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(GyrosphereVehicle.class, GyrosphereRenderer::new);
    }

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFencePole.class, new BlockEntityElectricFencePoleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFence.class, new BlockEntityElectricFenceRenderer());

    }

    private static class DinosaurRenderer extends AnimatableRenderer<DinosaurEntity, ModelStage> {

        DinosaurRenderer(RenderManager renderManagerIn) {
            super(renderManagerIn, entity -> entity.getDinosaur().getSystemInfo());
        }
    }
}
