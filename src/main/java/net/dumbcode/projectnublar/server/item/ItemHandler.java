package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public final class ItemHandler {

    @GameRegistry.ObjectHolder(ProjectNublar.MODID+":fossil")
    public static final FossilItem FOSSIL = getNonNull();
    public static final Map<Dinosaur, ItemDinosaurMeat> RAW_MEAT_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, ItemDinosaurMeat> COOKED_MEAT_ITEMS = new HashMap<>();

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {
        populateMap(RAW_MEAT_ITEMS, d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.RAW));
        populateMap(COOKED_MEAT_ITEMS, d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.COOKED));
        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection().stream()
                .filter(d -> d != Dinosaur.MISSING)
                .forEach(d -> {
                    ItemDinosaurMeat rawMeat = RAW_MEAT_ITEMS.get(d);
                    ItemDinosaurMeat cookedMeat = COOKED_MEAT_ITEMS.get(d);
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

    private static <T extends Item> void populateMap(Map<Dinosaur, T> itemMap, Function<Dinosaur, T> supplier) {
        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()
                .stream()
                .filter(d -> d != Dinosaur.MISSING)
                .forEach(d -> itemMap.put(d, supplier.apply(d)));
    }

//    @Nonnull
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
