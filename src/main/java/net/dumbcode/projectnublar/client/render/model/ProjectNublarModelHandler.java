package net.dumbcode.projectnublar.client.render.model;

import net.dumbcode.dumblibrary.client.component.ComponentRenderer;
import net.dumbcode.projectnublar.client.model.fossil.FossilBakedModel;
import net.dumbcode.projectnublar.client.model.fossil.FossilBlockItemBakedModel;
import net.dumbcode.projectnublar.client.model.fossil.FossilItemBakedModel;
import net.dumbcode.projectnublar.client.render.entity.DinosaurEggRenderer;
import net.dumbcode.projectnublar.client.render.entity.EntityPartRenderer;
import net.dumbcode.projectnublar.client.render.entity.GyrosphereRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ProjectNublar.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ProjectNublarModelHandler {

    private static final ResourceLocation ELECTRIC_FENCE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "block/electric_fence");
    private static TextureAtlasSprite electricFence;

    private static final ResourceLocation ELECTRIC_FENCE_WARNING_LOCATION = new ResourceLocation(ProjectNublar.MODID, "block/voltage_warning");
    private static TextureAtlasSprite electricFenceWarning;


    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if(PlayerContainer.BLOCK_ATLAS.equals(event.getMap().location())) {
            event.addSprite(ELECTRIC_FENCE_LOCATION);
            event.addSprite(ELECTRIC_FENCE_WARNING_LOCATION);
        }
    }

    @SubscribeEvent
    public static void onTextureStitched(TextureStitchEvent.Post event) {
        if(PlayerContainer.BLOCK_ATLAS.equals(event.getMap().location())) {
            electricFence = event.getMap().getSprite(ELECTRIC_FENCE_LOCATION);
            electricFenceWarning = event.getMap().getSprite(ELECTRIC_FENCE_WARNING_LOCATION);
        }
    }

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityHandler.DINOSAUR.get(), ComponentRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHandler.GYROSPHERE.get(), GyrosphereRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHandler.DUMMY_PART.get(), EntityPartRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHandler.DINOSAUR_EGG.get(), DinosaurEggRenderer::new);
//
//        ClientRegistry.bindTileEntitySpecialRenderer(SkeletalBuilderBlockEntity.class, new BlockEntitySkeletalBuilderRenderer());
//        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFencePole.class, new BlockEntityElectricFencePoleRenderer());
//        ClientRegistry.bindTileEntitySpecialRenderer(BlockEntityElectricFence.class, new BlockEntityElectricFenceRenderer());
//        ClientRegistry.bindTileEntitySpecialRenderer(IncubatorBlockEntity.class, new BlockEntityIncubatorRenderer());
//        ClientRegistry.bindTileEntitySpecialRenderer(EggPrinterBlockEntity.class, new BlockEntityEggPrinterRenderer());

    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();

        //TODO: convert this to a custom model wrapper.
        registry.put(
            BlockModelShapes.stateToModelLocation(BlockHandler.ELECTRIC_FENCE.get().defaultBlockState()),
            new FenceBakedModel(electricFence)
        );

        for(Block block : new Block[] { BlockHandler.LOW_SECURITY_ELECTRIC_FENCE_POLE.get(), BlockHandler.HIGH_SECURITY_ELECTRIC_FENCE_POLE.get() }) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                registry.computeIfPresent(BlockModelShapes.stateToModelLocation(state), (rl, model) -> new FencePoleBakedModel(electricFence, model));
            }
        }

        //TODO: convert this to a custom model wrapper.
        registry.put(
                BlockModelShapes.stateToModelLocation(BlockHandler.FOSSIL_BLOCK.get().defaultBlockState()),
                new FossilBakedModel()
        );

        registry.put(
                new ModelResourceLocation(BlockHandler.FOSSIL_BLOCK.get().getRegistryName(), "inventory"),
                new FossilBlockItemBakedModel()
        );

        registry.put(
                new ModelResourceLocation(Objects.requireNonNull(ItemHandler.FOSSIL_ITEM.get().getRegistryName()), "inventory"),
                new FossilItemBakedModel(resourceLocation -> event.getModelLoader().getModel(resourceLocation))
        );
    }

}
