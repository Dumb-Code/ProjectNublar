package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.animation.AnimatableRenderer;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFencePoleRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntityElectricFenceRenderer;
import net.dumbcode.projectnublar.client.render.blockentity.BlockEntitySkeletalBuilderRenderer;
import net.dumbcode.projectnublar.client.render.entity.EntityPartRenderer;
import net.dumbcode.projectnublar.client.render.entity.GyrosphereRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
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
            ResourceLocation regName = dinosaur.getRegName();
            for (ModelStage value : new ModelStage[] {ModelStage.ADULT, ModelStage.INFANT, ModelStage.CHILD, ModelStage.ADOLESCENCE, ModelStage.SKELETON}) {
                if(!dinosaur.getAttacher().getStorage(EntityComponentTypes.ANIMATION).getModelGrowthStages().contains(value)) {
                    dinosaur.getModelContainer().put(value, dinosaur.getModelContainer().get(ModelStage.ADULT));
                } else {
                    dinosaur.getModelContainer().put(value, new ModelContainer<>(new ResourceLocation(regName.getNamespace(), regName.getPath() + "_" + value), dinosaur.getSystemInfo().get(value)));
                }
            }
        }

        RenderingRegistry.registerEntityRenderingHandler(DinosaurEntity.class, DinosaurRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(GyrosphereVehicle.class, GyrosphereRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPart.class, EntityPartRenderer::new);
    }

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFencePole.class, new BlockEntityElectricFencePoleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFence.class, new BlockEntityElectricFenceRenderer());

    }

    private static class DinosaurRenderer extends AnimatableRenderer<DinosaurEntity> {

        DinosaurRenderer(RenderManager renderManagerIn) {
            super(renderManagerIn, entity -> entity.getDinosaur().getSystemInfo().get(entity.getState()));
        }

        @Override
        protected void preRenderCallback(DinosaurEntity entity, float partialTickTime) {
            if(entity.getCustomNameTag().equals("JTGhawk137"))
                GlStateManager.scale(0.1, 2.5, 2.5);
            else
                GlStateManager.scale(2.5,2.5,2.5);
            super.preRenderCallback(entity, partialTickTime);
        }
    }
}
