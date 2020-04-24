package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.tablet.TabletModuleHandler;
import net.dumbcode.projectnublar.server.tabs.TabHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public final class ItemHandler {

    public static final Item EMPTY_TEST_TUBE = new Item().setMaxStackSize(1);
    public static final Item IRON_FILTER = new FilterItem(0.25F);
    public static final Item GOLD_FILTER = new FilterItem(0.5F);
    public static final Item DIAMOND_FILTER = new FilterItem(1.0F);
    public static final Item AMBER = new Item();
    public static final Item HARD_DRIVE = new DriveItem(false);
    public static final Item SOLID_STATE_DRIVE = new DriveItem(true);
    public static final Item EMPTY_SYRINGE = new ItemSyringe(ItemSyringe.Type.EMPTY);
    public static final Item DNA_FILLED_SYRINGE = new ItemSyringe(ItemSyringe.Type.FILLED_DNA);
    public static final Item EMBRYO_FILLED_SYRINGE = new ItemSyringe(ItemSyringe.Type.FILLED_EMBRYO);
    public static final Item FENCE_REMOVER = new ItemFenceRemover();
    public static final Item CREATIVE_FENCE_REMOVER = new CreativeFenceRemovers();
    public static final Item ARTIFICIAL_EGG = new Item();
    public static final Item BROKEN_ARTIFICIAL_EGG = new Item();

    public static final Item TRACKING_MODULE = new BasicModuleItem(() -> TabletModuleHandler.TRACKING_TABLET);
    public static final Item FLAPPY_DINO_MODULE = new BasicModuleItem(() -> TabletModuleHandler.FLAPPY_DINO);

    public static final Item TRACKING_TABLET = new ItemTrackingTablet();

    public static final Item COMPUTER_CHIP_PART = new ItemMetaNamed(3);
    public static final Item TANKS_PART = new ItemMetaNamed(4);
    public static final Item DRILL_BIT_PART = new ItemMetaNamed(5);
    public static final Item LEVELLING_SENSOR_PART = new Item();
    public static final Item BULB_PART = new ItemMetaNamed(3);
    public static final Item CONTAINER_PART = new ItemMetaNamed(2);
    public static final Item TURBINES_PART = new ItemMetaNamed(2);

    public static final Map<Dinosaur, ItemDinosaurMeat> RAW_MEAT_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, ItemDinosaurMeat> COOKED_MEAT_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, DinosaurSpawnEgg> SPAWN_EGG_ITEMS = new HashMap<>();
    public static final Map<Dinosaur, DinosaurGeneticMaterialItem> TEST_TUBES_GENETIC_MATERIAL = new HashMap<>();
    public static final Map<Dinosaur, BasicDinosaurItem>  TEST_TUBES_DNA = new HashMap<>();
    public static final Map<Dinosaur, DinosaurTooltipItem> DINOSAUR_UNINCUBATED_EGG = new HashMap<>();
    public static final Map<Dinosaur, BasicDinosaurItem> DINOSAUR_INCUBATED_EGG = new HashMap<>();

    public static final Map<Dinosaur, Map<String, FossilItem>> FOSSIL_ITEMS = new HashMap<>();

    private static final CreativeTabs TAB = TabHandler.TAB;

    @SubscribeEvent
    public static void onItemRegistry(RegistryEvent.Register<Item> event) {

        event.getRegistry().registerAll(
            EMPTY_TEST_TUBE.setRegistryName("test_tube").setTranslationKey("test_tube").setCreativeTab(TAB),
            IRON_FILTER.setRegistryName("iron_filter").setTranslationKey("iron_filter").setCreativeTab(TAB).setMaxDamage(150),
            GOLD_FILTER.setRegistryName("gold_filter").setTranslationKey("gold_filter").setCreativeTab(TAB).setMaxDamage(250),
            DIAMOND_FILTER.setRegistryName("diamond_filter").setTranslationKey("diamond_filter").setCreativeTab(TAB).setMaxDamage(500),
            AMBER.setRegistryName("amber").setTranslationKey("amber").setCreativeTab(TAB),
            HARD_DRIVE.setRegistryName("hard_drive").setTranslationKey("hard_drive").setCreativeTab(TAB).setMaxStackSize(1),
            SOLID_STATE_DRIVE.setRegistryName("solid_state_drive").setTranslationKey("solid_state_drive").setCreativeTab(TAB).setMaxStackSize(1),
            EMPTY_SYRINGE.setRegistryName("empty_syringe").setTranslationKey("empty_syringe").setCreativeTab(TAB),
            DNA_FILLED_SYRINGE.setRegistryName("dna_filled_syringe").setTranslationKey("dna_filled_syringe").setCreativeTab(TAB),
            EMBRYO_FILLED_SYRINGE.setRegistryName("embryo_filled_syringe").setTranslationKey("embryo_filled_syringe").setCreativeTab(TAB),
            FENCE_REMOVER.setRegistryName("fence_remover").setTranslationKey("fence_remover").setCreativeTab(TAB),
            CREATIVE_FENCE_REMOVER.setRegistryName("creative_fence_remover").setTranslationKey("creative_fence_remover").setCreativeTab(TAB),
            ARTIFICIAL_EGG.setRegistryName("artificial_egg").setTranslationKey("artificial_egg").setCreativeTab(TAB),
            BROKEN_ARTIFICIAL_EGG.setRegistryName("broken_artificial_egg").setTranslationKey("broken_artificial_egg").setCreativeTab(TAB),
            TRACKING_TABLET.setRegistryName("tracking_tablet").setTranslationKey("tracking_tablet").setCreativeTab(TAB),
            TRACKING_MODULE.setRegistryName("tracking_module").setTranslationKey("tracking_module").setCreativeTab(TAB),
            FLAPPY_DINO_MODULE.setRegistryName("flappy_dino_module").setTranslationKey("flappy_dino_module").setCreativeTab(TAB),

            COMPUTER_CHIP_PART.setRegistryName("computer_chip_part").setTranslationKey("computer_chip_part").setCreativeTab(TAB),
            TANKS_PART.setRegistryName("tanks_part").setTranslationKey("tanks_part").setCreativeTab(TAB),
            DRILL_BIT_PART.setRegistryName("drill_bit_part").setTranslationKey("drill_bit_part").setCreativeTab(TAB),
            LEVELLING_SENSOR_PART.setRegistryName("levelling_sensor_part").setTranslationKey("levelling_sensor_part").setCreativeTab(TAB),
            BULB_PART.setRegistryName("bulb_part").setTranslationKey("bulb_part").setCreativeTab(TAB),
            CONTAINER_PART.setRegistryName("container_part").setTranslationKey("container_part").setCreativeTab(TAB)
        );

        UnaryOperator<Item> tab = item -> item.setCreativeTab(TAB);

        populateMap(event, RAW_MEAT_ITEMS, "%s_meat_dinosaur_raw", d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.RAW), tab);
        populateMap(event, COOKED_MEAT_ITEMS, "%s_meat_dinosaur_cooked", d -> new ItemDinosaurMeat(d, ItemDinosaurMeat.CookState.COOKED), tab);
        populateMap(event, SPAWN_EGG_ITEMS, "%s_spawn_egg", DinosaurSpawnEgg::new, tab);
        populateMap(event, TEST_TUBES_GENETIC_MATERIAL, "%s_genetic_material_test_tube", DinosaurGeneticMaterialItem::new, tab.andThen(i -> i.setMaxStackSize(1)));
        populateMap(event, TEST_TUBES_DNA, "%s_test_tube", BasicDinosaurItem::new);
        populateMap(event, DINOSAUR_UNINCUBATED_EGG, "%s_unincubated_egg", d -> new DinosaurTooltipItem(d, stack -> Lists.newArrayList(stack.getOrCreateSubCompound(ProjectNublar.MODID).getInteger("AmountDone") + "%")));
        populateMap(event, DINOSAUR_INCUBATED_EGG, "%s_incubated_egg", DinosaurEggItem::new);

        populateNestedMap(event, FOSSIL_ITEMS, dino -> dino.getAttacher().getStorage(ComponentHandler.ITEM_DROPS).getFossilList(), FossilItem::new, "%s_fossil_%s", tab);


        for (Block block : ForgeRegistries.BLOCKS) {
            if(block instanceof IItemBlock) {
                event.getRegistry().register(((IItemBlock)block).createItem()
                        .setRegistryName(Objects.requireNonNull(block.getRegistryName()))
                        .setTranslationKey(block.getTranslationKey().substring("tile.".length()))
                        .setCreativeTab(block.getCreativeTab()));
            }
        }

        //TODO: iterate through all items, and check the cast
        registerOreNames(RAW_MEAT_ITEMS);
        registerOreNames(COOKED_MEAT_ITEMS);
        FOSSIL_ITEMS.values().forEach(ItemHandler::registerOreNames);
    }

    /**
     * Needs to be called **after** registering all the items
     * @param items
     */
    private static void registerOreNames(Map<?, ? extends Item> items) {
        items.values().stream()
                .filter(ItemWithOreName.class::isInstance)
                .map(ItemWithOreName.class::cast)
                .forEach(ItemWithOreName::registerOreNames);
    }

    private static <T extends Item> void populateMap(RegistryEvent.Register<Item> event, Map<Dinosaur, T> itemMap, String dinosaurRegname, Function<Dinosaur, T> supplier) {
        populateMap(event, itemMap, dinosaurRegname, supplier, null);
    }

    private static <T extends Item> void populateMap(RegistryEvent.Register<Item> event, Map<Dinosaur, T> itemMap, String dinosaurRegname, Function<Dinosaur, T> supplier, @Nullable Function<Item, Item> initializer) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {

            T item = supplier.apply(dinosaur);
            String name = String.format(dinosaurRegname, dinosaur.getFormattedName());
            item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
            item.setTranslationKey(name);
            if(initializer != null) {
                item = runInitilizer(item, initializer);
            }
            itemMap.put(dinosaur, item);
            event.getRegistry().register(item);
        }
    }

    @Deprecated
    private static <T extends Item, S> void populateNestedMap(RegistryEvent.Register<Item> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        populateNestedMap(event, itemMap, getterFunction, Object::toString, creationFunc, dinosaurRegname, null);
    }

    private static <T extends Item, S> void populateNestedMap(RegistryEvent.Register<Item> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname, @Nullable Function<Item, Item> initializer) {
        populateNestedMap(event, itemMap, getterFunction, Object::toString, creationFunc, dinosaurRegname, initializer);
    }

    private static <T extends Item, S> void populateNestedMap(RegistryEvent.Register<Item> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, Function<S, String> toStringFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname, @Nullable Function<Item, Item> initializer) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            for (S s : getterFunction.apply(dinosaur)) {
                T item = creationFunc.apply(dinosaur, s);
                String name = String.format(dinosaurRegname, dinosaur.getFormattedName(), toStringFunction.apply(s));
                item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                item.setTranslationKey(name);
                if(initializer != null) {
                    item = runInitilizer(item, initializer);
                }
                itemMap.computeIfAbsent(dinosaur, d -> new HashMap<>()).put(s, item);
                event.getRegistry().register(item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> T runInitilizer(T item, Function<Item, Item> initializer) {
        Item initializedItem = initializer.apply(item);
        if(!item.getClass().isInstance(initializedItem)) {
            throw new RuntimeException("Initialized class did not give same as (of subclass of) initial class. Initial: " + item.getClass() + " Initialized: " + initializedItem.getClass());
        }
        return (T) initializedItem;
    }
}
