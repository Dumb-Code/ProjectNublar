package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.component.ComponentRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.*;
import net.dumbcode.projectnublar.client.render.entity.DinosaurEggRenderer;
import net.dumbcode.projectnublar.client.render.entity.EntityPartRenderer;
import net.dumbcode.projectnublar.client.render.entity.GyrosphereRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.entity.DinosaurEggEntity;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.dumbcode.projectnublar.server.recipes.EggPrinterRecipe;
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
        RenderingRegistry.registerEntityRenderingHandler(DinosaurEggEntity.class, DinosaurEggRenderer::new);

        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFencePole.class, new BlockEntityElectricFencePoleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFence.class, new BlockEntityElectricFenceRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(IncubatorBlockEntity.class, new BlockEntityIncubatorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(EggPrinterBlockEntity.class, new BlockEntityEggPrinterRenderer());

        EnumDinosaurEggTypes.registerResourceReload();
    }

}
