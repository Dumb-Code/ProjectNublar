package net.dumbcode.projectnublar.server.recipes.crafting;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class VanillaCraftingHandler {

    @SubscribeEvent
    public static void register(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().registerAll(
                new EggPrinterCraftingRecipe().setRegistryName("artificial_egg")
        );
    }

}
