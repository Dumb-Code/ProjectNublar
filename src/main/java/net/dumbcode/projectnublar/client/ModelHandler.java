package net.dumbcode.projectnublar.client;

import net.dumbcode.dumblibrary.client.model.tabula.baked.TabulaModelHandler;
import net.dumbcode.projectnublar.client.commandmodel.ModelCommandLoader;
import net.dumbcode.projectnublar.client.render.FenceStateMapper;
import net.dumbcode.projectnublar.client.utils.FullAtlasSprite;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;
import java.util.Objects;

import static net.dumbcode.projectnublar.server.block.BlockHandler.*;
import static net.dumbcode.projectnublar.server.item.ItemHandler.*;

//TODO:
//  ModelLoaderRegistry#registerLoader
//  register a custom model loader in order to have the same model have different dinosaur symbols
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class ModelHandler {

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {

        ModelLoader.setCustomStateMapper(LOW_SECURITY_ELECTRIC_FENCE_POLE, new FenceStateMapper(LOW_SECURITY_ELECTRIC_FENCE_POLE));
        ModelLoader.setCustomStateMapper(HIGH_SECURITY_ELECTRIC_FENCE_POLE, new FenceStateMapper(HIGH_SECURITY_ELECTRIC_FENCE_POLE));

        TabulaModelHandler.INSTANCE.allow(ProjectNublar.MODID);
        ModelLoaderRegistry.registerLoader(ModelCommandLoader.INSTANCE);

        reg(AMBER, HARD_DRIVE, EMPTY_SYRINGE, EMBRYO_FILLED_SYRINGE, DNA_FILLED_SYRINGE, EMPTY_TEST_TUBE, Item.getItemFromBlock(HIGH_SECURITY_ELECTRIC_FENCE_POLE),
                Item.getItemFromBlock(LOW_SECURITY_ELECTRIC_FENCE_POLE), Item.getItemFromBlock(ELECTRIC_FENCE), Item.getItemFromBlock(CREATIVE_POWER_SOURCE));
        reg(TEST_TUBES_GENETIC_MATERIAL);
    }

    private static void reg(Item... items) {
        for (Item item : items) {
            reg(item);
        }
    }

    private static void reg(Map<?, ? extends Item> map) {
        for (Item item : map.values()) {
            reg(item);
        }
    }

    private static void reg(Item item) {
        reg(item, Objects.requireNonNull(item.getRegistryName()));
    }

    private static void reg(Item item, ResourceLocation location) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory"));
    }

    public static IBakedModel getModel(ResourceLocation resourceLocation, TextureStitchEvent event, VertexFormat format) throws Exception {
        return ModelLoaderRegistry.getModel(resourceLocation).bake(TRSRTransformation.identity(), format, FullAtlasSprite::new);
    }

    private static IBakedModel disableAO(IBakedModel model) {
        return new BakedModelWrapper<IBakedModel>(model) {
            @Override
            public boolean isAmbientOcclusion() {
                return false;
            }
        };
    }

}
