package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.data.FossilInformation;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public final class ItemHandler {

    public static final Map<Dinosaur, ItemDinosaurMeat> RAW_MEAT_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, ItemDinosaurMeat> COOKED_MEAT_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, Collection<FossilItem>> FOSSIL_ITEMS = new HashMap<>();

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        populateMap(event, RAW_MEAT_ITEMS, d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.RAW));
        populateMap(event, COOKED_MEAT_ITEMS, d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.COOKED));
        populateCollectionMap(event, FOSSIL_ITEMS, d -> {
            List<FossilInformation> information = d.getFossilInformation();
            return information.stream()
                    .map(info -> {
                        FossilItem fossil = new FossilItem(d, info);
                        String id = "fossil_"+d.getRegName().getResourcePath()+"_"+info.getType().toLowerCase();
                        fossil.setRegistryName(new ResourceLocation(ProjectNublar.MODID, id))
                                .setUnlocalizedName(id)
                                .setCreativeTab(ProjectNublar.TAB);
                        return fossil;
                    })
                    .collect(Collectors.toList());
        });
        registerOreNames(RAW_MEAT_ITEMS);
        registerOreNames(COOKED_MEAT_ITEMS);
        registerCollectionOreNames(FOSSIL_ITEMS);
        event.getRegistry().registerAll(
                new DinosaurSpawnEgg()
                        .setRegistryName("spawn_egg")
                        .setUnlocalizedName("spawn_egg")
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

    /**
     * Needs to be called **after** registering all the items
     * @param items
     */
    private static void registerOreNames(Map<Dinosaur, ? extends Item> items) {
        items.values().stream()
                .filter(item -> item instanceof ItemWithOreName)
                .forEach(item -> ((ItemWithOreName)item).registerOreNames());
    }

    /**
     * Needs to be called **after** registering all the items
     * @param items
     */
    private static <T extends Item> void registerCollectionOreNames(Map<Dinosaur, Collection<T>> items) {
        items.values()
                .forEach(collection ->
                        collection.stream()
                                .filter(item -> item instanceof ItemWithOreName)
                                .forEach(item -> ((ItemWithOreName)item).registerOreNames()
                                ));
    }

    private static <T extends Item> void populateCollectionMap(RegistryEvent.Register<Item> event, Map<Dinosaur, Collection<T>> itemMap, Function<Dinosaur, Collection<T>> supplier) {
        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()
                .stream()
                .filter(d -> d != Dinosaur.MISSING)
                .forEach(d -> {
                    Collection<T> item = supplier.apply(d);
                    itemMap.put(d, item);
                    item.forEach(event.getRegistry()::register);
                });
    }

    private static <T extends Item> void populateMap(RegistryEvent.Register<Item> event, Map<Dinosaur, T> itemMap, Function<Dinosaur, T> supplier) {
        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()
                .stream()
                .filter(d -> d != Dinosaur.MISSING)
                .forEach(d -> {
                    T item = supplier.apply(d);
                    itemMap.put(d, item);
                    event.getRegistry().register(item);
                });
    }

//    @Nonnull
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
