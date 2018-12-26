package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;
import java.util.Objects;

import static net.dumbcode.projectnublar.server.item.ItemHandler.*;
import static net.dumbcode.projectnublar.server.block.BlockHandler.*;

//TODO:
//  ModelLoaderRegistry#registerLoader
//  register a custom model loader in order to have the same model have different dinosaur symbols
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class ModelHandler {

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

}
