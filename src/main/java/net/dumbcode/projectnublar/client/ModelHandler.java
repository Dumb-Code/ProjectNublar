package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelBakeEvent;
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

import static net.dumbcode.projectnublar.server.block.BlockHandler.HIGH_SECURITY_ELECTRIC_FENCE_POLE;
import static net.dumbcode.projectnublar.server.block.BlockHandler.LIGHT_STEEL_ELECTRIC_FENCE_POLE;
import static net.dumbcode.projectnublar.server.item.ItemHandler.*;

//TODO:
//  ModelLoaderRegistry#registerLoader
//  register a custom model loader in order to have the same model have different dinosaur symbols
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class ModelHandler {

    public static IBakedModel LIGHT_STEEL;
    public static IBakedModel HIGH_SECURITY;

    public static TextureAtlasSprite FENCE_SPRITE;

    @SubscribeEvent
    public static void onTextureStitched(TextureStitchEvent event) {
        try {
            LIGHT_STEEL = disableAO(getModel(new ResourceLocation(ProjectNublar.MODID, "block/light_steel_electric_fence_pole.tbl"), event, DefaultVertexFormats.POSITION_TEX_NORMAL));
            HIGH_SECURITY = disableAO(getModel(new ResourceLocation(ProjectNublar.MODID, "block/high_security_electric_fence_pole.tbl"), event, DefaultVertexFormats.POSITION_TEX_NORMAL));
        } catch (Exception e) {
            e.printStackTrace();
        }
        FENCE_SPRITE = event.getMap().registerSprite(new ResourceLocation(ProjectNublar.MODID, "blocks/electric_fence"));
    }

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        TabulaModelHandler.INSTANCE.addDomain(ProjectNublar.MODID);

        ModelLoader.setCustomStateMapper(HIGH_SECURITY_ELECTRIC_FENCE_POLE, (new StateMap.Builder().ignore(BlockElectricFencePole.INDEX_PROPERTY)).build());
        ModelLoader.setCustomStateMapper(LIGHT_STEEL_ELECTRIC_FENCE_POLE, (new StateMap.Builder().ignore(BlockElectricFencePole.INDEX_PROPERTY)).build());


        reg(AMBER, HARD_DRIVE, EMPTY_SYRINGE, EMBRYO_FILLED_SYRINGE, DNA_FILLED_SYRINGE, EMPTY_TEST_TUBE);
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
        return ModelLoaderRegistry.getModel(resourceLocation).bake(TRSRTransformation.identity(), format, event.getMap()::registerSprite);
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
