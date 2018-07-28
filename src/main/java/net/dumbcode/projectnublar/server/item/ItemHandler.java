package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.CachedItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public final class ItemHandler {

    public static final ItemDinosaurMeat DINOSAUR_MEAT = getNonNull();
    public static final DinosaurSpawnEgg SPAWN_EGG = getNonNull();
    public static final FossilItem FOSSIL = getNonNull();

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        System.out.println("ITEM REGISTER");
        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection().stream()
                .filter(d -> d != Dinosaur.MISSING)
                .forEach(d -> {
                    d.setCachedItems(new CachedItems(d));
                    System.out.println("HELLO "+d.getRegName());
                    ItemDinosaurMeat rawMeat = d.getCachedItems().getRawMeat();
                    ItemDinosaurMeat cookedMeat = d.getCachedItems().getCookedMeat();
                    event.getRegistry().registerAll(rawMeat, cookedMeat);
                    rawMeat.registerOreNames();
                    cookedMeat.registerOreNames();
                });
        event.getRegistry().registerAll(
                new DinosaurSpawnEgg()
                        .setRegistryName("spawn_egg")
                        .setUnlocalizedName("spawn_egg")
                        .setCreativeTab(ProjectNublar.TAB),

                new FossilItem()
                        .setRegistryName("fossil")
                        .setUnlocalizedName("fossil")
                        .setCreativeTab(ProjectNublar.TAB)
        );
        for (Block block : ForgeRegistries.BLOCKS) {
            if(block instanceof IItemBlock) {
                event.getRegistry().register(((IItemBlock)block).createItem()
                        .setRegistryName(block.getRegistryName())
                        .setUnlocalizedName(block.getUnlocalizedName().substring("tile.".length()))
                        .setCreativeTab(block.getCreativeTabToDisplayOn()));
            }
        }
    }

//    @Nonnull
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
