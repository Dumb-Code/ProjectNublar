package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.IItemBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurDropsComponent;
import net.dumbcode.dumblibrary.server.registry.PreprocessRegisterDeferredRegister;
import net.dumbcode.projectnublar.server.tablet.TabletModuleHandler;
import net.dumbcode.projectnublar.server.tabs.TabHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public final class ItemHandler {

    private static final ItemGroup TAB = TabHandler.TAB;

    private static final Supplier<Item> BASIC_ITEM = () -> new Item(new Item.Properties().tab(TAB));

    public static final PreprocessRegisterDeferredRegister<Item> REGISTER = PreprocessRegisterDeferredRegister.create(ForgeRegistries.ITEMS.getRegistrySuperType(), ProjectNublar.MODID);

    public static final RegistryObject<Item> EMPTY_TEST_TUBE = REGISTER.register("test_tube", () -> new Item(new Item.Properties().stacksTo(1).tab(TAB)));
    public static final RegistryObject<Item> IRON_FILTER = REGISTER.register("iron_filter", () -> new FilterItem(0.25F, new Item.Properties().durability(150).tab(TAB)));
    public static final RegistryObject<Item> GOLD_FILTER = REGISTER.register("gold_filter", () -> new FilterItem(0.5F, new Item.Properties().durability(250).tab(TAB)));
    public static final RegistryObject<Item> DIAMOND_FILTER = REGISTER.register("diamond_filter", () -> new FilterItem(1.0F, new Item.Properties().durability(500).tab(TAB)));
    public static final RegistryObject<Item> AMBER = REGISTER.register("amber", BASIC_ITEM);
    public static final RegistryObject<Item> HARD_DRIVE = REGISTER.register("hard_drive", () -> new DriveItem(false, new Item.Properties().stacksTo(1).tab(TAB)));
    public static final RegistryObject<Item> SOLID_STATE_DRIVE = REGISTER.register("solid_state_drive", () -> new DriveItem(true, new Item.Properties().stacksTo(1).tab(TAB)));
    public static final RegistryObject<Item> EMPTY_SYRINGE = REGISTER.register("empty_syringe", () -> new ItemSyringe(ItemSyringe.Type.EMPTY, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> DNA_FILLED_SYRINGE = REGISTER.register("dna_filled_syringe", () -> new ItemSyringe(ItemSyringe.Type.FILLED_DNA, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> EMBRYO_FILLED_SYRINGE = REGISTER.register("embryo_filled_syringe", () -> new ItemSyringe(ItemSyringe.Type.FILLED_EMBRYO, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> FENCE_REMOVER = REGISTER.register("fence_remover", () -> new ItemFenceRemover(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> CREATIVE_FENCE_REMOVER = REGISTER.register("creative_fence_remover", () -> new CreativeFenceRemovers(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> ARTIFICIAL_EGG = REGISTER.register("artificial_egg", BASIC_ITEM);
    public static final RegistryObject<Item> BROKEN_ARTIFICIAL_EGG = REGISTER.register("broken_artificial_egg", () ->new Item(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> WIRE_SPOOL = REGISTER.register("wire_spool", () -> new WireSpoolItem(BlockHandler.ELECTRIC_FENCE.get(), new Item.Properties()));

    public static final RegistryObject<Item> TABLET = REGISTER.register("tablet", () -> new ItemTablet(new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> TRACKING_MODULE = REGISTER.register("tracking_module", () -> new BasicModuleItem(TabletModuleHandler.TRACKING_TABLET, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> FLAPPY_DINO_MODULE = REGISTER.register("flappy_dino_module", () -> new BasicModuleItem(TabletModuleHandler.FLAPPY_DINO, new Item.Properties().tab(TAB)));

    public static final RegistryObject<Item> COMPUTER_CHIP_PART_1 = REGISTER.register("computer_chip_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> COMPUTER_CHIP_PART_2 = REGISTER.register("computer_chip_part_2", BASIC_ITEM);
    public static final RegistryObject<Item> COMPUTER_CHIP_PART_3 = REGISTER.register("computer_chip_part_3", BASIC_ITEM);

    public static final RegistryObject<Item> TANKS_PART_1 = REGISTER.register("tanks_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> TANKS_PART_2 = REGISTER.register("tanks_part_2", BASIC_ITEM);
    public static final RegistryObject<Item> TANKS_PART_3 = REGISTER.register("tanks_part_3", BASIC_ITEM);
    public static final RegistryObject<Item> TANKS_PART_4 = REGISTER.register("tanks_part_4", BASIC_ITEM);

    public static final RegistryObject<Item> DRILL_BIT_PART_1 = REGISTER.register("drill_bit_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> DRILL_BIT_PART_2 = REGISTER.register("drill_bit_part_2", BASIC_ITEM);
    public static final RegistryObject<Item> DRILL_BIT_PART_3 = REGISTER.register("drill_bit_part_3", BASIC_ITEM);
    public static final RegistryObject<Item> DRILL_BIT_PART_4 = REGISTER.register("drill_bit_part_4", BASIC_ITEM);
    public static final RegistryObject<Item> DRILL_BIT_PART_5 = REGISTER.register("drill_bit_part_5", BASIC_ITEM);

    public static final RegistryObject<Item> LEVELLING_SENSOR_PART = REGISTER.register("levelling_sensor_part", BASIC_ITEM);

    public static final RegistryObject<Item> BULB_PART_1 = REGISTER.register("bulb_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> BULB_PART_2 = REGISTER.register("bulb_part_2", BASIC_ITEM);
    public static final RegistryObject<Item> BULB_PART_3 = REGISTER.register("bulb_part_3", BASIC_ITEM);

    public static final RegistryObject<Item> CONTAINER_PART_1 = REGISTER.register("container_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> CONTAINER_PART_2 = REGISTER.register("container_part_2", BASIC_ITEM);
    public static final RegistryObject<Item> CONTAINER_PART_3 = REGISTER.register("container_part_3", BASIC_ITEM);

    public static final RegistryObject<Item> TURBINES_PART_1 = REGISTER.register("turbines_part_1", BASIC_ITEM);
    public static final RegistryObject<Item> TURBINES_PART_2 = REGISTER.register("turbines_part_2", BASIC_ITEM);

    public static final Map<Dinosaur, RegistryObject<Item>> RAW_MEAT_ITEMS = createMap("%s_raw_meat", d ->
        new BasicDinosaurItem(d, new Item.Properties().food(new Food.Builder()
            .nutrition(d.getItemProperties().getRawMeatHealAmount())
            .saturationMod(d.getItemProperties().getRawMeatSaturation())
            .build()
        ))
    );
    public static final Map<Dinosaur, RegistryObject<Item>> COOKED_MEAT_ITEMS = createMap("%s_cooked_meat", d ->
        new BasicDinosaurItem(d, new Item.Properties().food(new Food.Builder()
            .nutrition(d.getItemProperties().getRawMeatHealAmount())
            .saturationMod(d.getItemProperties().getRawMeatSaturation())
            .build()
        ))
    );

    public static final Map<Dinosaur, RegistryObject<Item>> SPAWN_EGG_ITEMS = createMap("%s_spawn_egg", d -> new DinosaurSpawnEgg(d, new Item.Properties()));
    public static final Map<Dinosaur, RegistryObject<Item>> TEST_TUBES_GENETIC_MATERIAL = createMap("%s_genetic_material_test_tube", d -> new DinosaurGeneticMaterialItem(d, new Item.Properties()));
    public static final Map<Dinosaur, RegistryObject<Item>> TEST_TUBES_DNA = createMap("%s_test_tube", d -> new BasicDinosaurItem(d, new Item.Properties()));
    public static final Map<Dinosaur, RegistryObject<Item>> DINOSAUR_UNINCUBATED_EGG = createMap("%s_unincubated_egg", d -> new UnincubatedEggItem(d, new Item.Properties()));
    public static final Map<Dinosaur, RegistryObject<Item>> DINOSAUR_INCUBATED_EGG = createMap("%s_incubated_egg", d -> new DinosaurEggItem(d, new Item.Properties()));

    public static final Map<Dinosaur, Map<String, RegistryObject<Item>>> FOSSIL_ITEMS = createNestedMap(
        "%s_fossil_%s",
        dino -> JavaUtils.nullOr(dino.getAttacher().getStorageOrNull(ComponentHandler.ITEM_DROPS.get()), DinosaurDropsComponent.Storage::getFossilList),
        (dinosaur, fossil) -> new FossilItem(dinosaur, fossil, new Item.Properties())
        );

    public static void registerAllItemBlocks(RegistryEvent.Register<Item> event) {
        for (RegistryObject<Block> entry : BlockHandler.REGISTER.getEntries()) {
            Block block = entry.get();
            if(block instanceof IItemBlock) {
                Item item = ((IItemBlock) block).createItem(new Item.Properties().tab(TAB));
                item.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
                event.getRegistry().register(item);
            }
        }
    }

    private static <T extends Item> Map<Dinosaur, RegistryObject<T>> createMap(String format, Function<Dinosaur, T> supplier) {
        Map<Dinosaur, RegistryObject<T>> map = new HashMap<>();
        REGISTER.beforeRegister(() -> {
            for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
                map.put(dinosaur, REGISTER.register(
                    String.format(format, dinosaur.getFormattedName()),
                    () -> supplier.apply(dinosaur)
                ));
            }
        });
        return map;
    }

    private static <T extends Item, S> Map<Dinosaur, Map<S, RegistryObject<T>>> createNestedMap(
        String format,
        Function<Dinosaur, Collection<S>> getterFunction,
        BiFunction<Dinosaur, S, T> creationFunc
    ) {
        return createNestedMap(format, getterFunction, Object::toString, creationFunc);
    }

    private static <T extends Item, S> Map<Dinosaur, Map<S, RegistryObject<T>>> createNestedMap(
        String format,
        Function<Dinosaur, Collection<S>> getterFunction,
        Function<S, String> toStringFunction,
        BiFunction<Dinosaur, S, T> creationFunc
    ) {
        Map<Dinosaur, Map<S, RegistryObject<T>>> map = new HashMap<>();
        REGISTER.beforeRegister(() -> {
            for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
                Collection<S> collection = getterFunction.apply(dinosaur);
                if (collection != null) {
                    for (S s : collection) {
                        map.computeIfAbsent(dinosaur, d -> new HashMap<>()).put(s, REGISTER.register(
                            String.format(format, dinosaur.getFormattedName(), toStringFunction.apply(s)),
                            () -> creationFunc.apply(dinosaur, s)
                        ));
                    }
                } else {
                    map.put(dinosaur, new HashMap<>());
                }
            }
        });
        return map;
    }
}
