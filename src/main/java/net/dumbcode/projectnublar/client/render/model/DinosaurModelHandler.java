package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.component.ComponentRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFencePoleRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFenceRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntitySkeletalBuilderRenderer;
import net.dumbcode.projectnublar.client.render.entity.EntityPartRenderer;
import net.dumbcode.projectnublar.client.render.entity.GyrosphereRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
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
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, ComponentRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(GyrosphereVehicle.class, GyrosphereRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPart.class, EntityPartRenderer::new);
    }

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFencePole.class, new BlockEntityElectricFencePoleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFence.class, new BlockEntityElectricFenceRenderer());

    }
}
