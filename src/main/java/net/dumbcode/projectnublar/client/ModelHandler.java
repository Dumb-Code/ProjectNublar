package net.dumbcode.projectnublar.client;

import net.dumbcode.dumblibrary.client.model.tabula.baked.TabulaModelHandler;
import net.dumbcode.projectnublar.client.render.FenceStateMapper;
import net.dumbcode.projectnublar.client.utils.FullAtlasSprite;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemMetaNamed;
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
//  OR: we can have it so if no models are explicitly defined, then we automatically infer the model from the base texture + overlay.
//  We'd need our own model system to take in a Map and populate all the entries
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class ModelHandler {

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {

        ModelLoader.setCustomStateMapper(LOW_SECURITY_ELECTRIC_FENCE_POLE, new FenceStateMapper(LOW_SECURITY_ELECTRIC_FENCE_POLE));
        ModelLoader.setCustomStateMapper(HIGH_SECURITY_ELECTRIC_FENCE_POLE, new FenceStateMapper(HIGH_SECURITY_ELECTRIC_FENCE_POLE));

        TabulaModelHandler.INSTANCE.allow(ProjectNublar.MODID);

        reg(AMBER, HARD_DRIVE, EMPTY_SYRINGE, EMBRYO_FILLED_SYRINGE, DNA_FILLED_SYRINGE, EMPTY_TEST_TUBE, Item.getItemFromBlock(HIGH_SECURITY_ELECTRIC_FENCE_POLE),
                Item.getItemFromBlock(LOW_SECURITY_ELECTRIC_FENCE_POLE), Item.getItemFromBlock(ELECTRIC_FENCE), Item.getItemFromBlock(CREATIVE_POWER_SOURCE));
        reg(TEST_TUBES_GENETIC_MATERIAL, "test_tube_genetic_material");


        for (Dinosaur dinosaur : FOSSIL_ITEMS.keySet()) {
            Map<String, FossilItem> itemMap = FOSSIL_ITEMS.get(dinosaur);
            for (String bone : itemMap.keySet()) {
                FossilItem item = itemMap.get(bone);
                reg(item, new ResourceLocation(dinosaur.getRegName().getNamespace(), "fossils/" + dinosaur.getRegName().getPath() + "/fossil_" + dinosaur.getRegName().getPath() + "_" + bone));
            }
        }

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

    //TODO: make overlay system
    private static void reg(Map<?, ? extends Item> map, String name) {
        for (Item item : map.values()) {
            reg(item, new ResourceLocation(Objects.requireNonNull(item.getRegistryName()).getNamespace(), name));
        }
    }

    private static void reg(Item item) {
        reg(item, Objects.requireNonNull(item.getRegistryName()));
    }

    private static void reg(Item item, ResourceLocation location) {
        if(item instanceof ItemMetaNamed) {
            for (int i = 0; i < ((ItemMetaNamed) item).getSubTypes(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(new ResourceLocation(location.getNamespace(), location.getPath() + "_" + i), "inventory"));
            }
        } else {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory"));
        }
    }

}
