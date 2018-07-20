package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public final class NublarItems {

    public static final ItemDinosaurMeat DINOSAUR_MEAT = null;
    public static final DinosaurSpawnEgg SPAWN_EGG = null;

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemDinosaurMeat()
                        .setRegistryName(new ResourceLocation(ProjectNublar.MODID, "dinosaur_meat"))
                        .setUnlocalizedName("dinosaur_meat")
                        .setCreativeTab(ProjectNublar.TAB),

                new DinosaurSpawnEgg()
                        .setRegistryName(new ResourceLocation(ProjectNublar.MODID, "spawn_egg"))
                        .setUnlocalizedName("spawn_egg")
                        .setCreativeTab(ProjectNublar.TAB)
        );
    }
    public static Item[] getAllItems() {
        return new Item[] {
                DINOSAUR_MEAT,
        };
    }
}
