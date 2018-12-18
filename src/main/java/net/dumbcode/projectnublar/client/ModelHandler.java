package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.Objects;

import static net.dumbcode.projectnublar.server.item.ItemHandler.*;

//TODO:
//  ModelLoaderRegistry#registerLoader
//  register a custom model loader in order to have the same model have different dinosaur symbols
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class ModelHandler {

    @SubscribeEvent
    public static void onModelRead(ModelRegistryEvent event) {
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
