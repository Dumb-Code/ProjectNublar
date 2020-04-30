package net.dumbcode.projectnublar.server.villager;

import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.ItemMetaNamed;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.NAME)
public class VillagerHandler {

    public static final VillagerRegistry.VillagerProfession PALEONTOLOGIST = InjectedUtils.injected();

    @SubscribeEvent
    public static void registerProfessions(RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
        event.getRegistry().register(
            createProfession("paleontologist", profession -> {
                VillagerRegistry.VillagerCareer career = new VillagerRegistry.VillagerCareer(profession, "paleontologist");
                career.addTrade(1,
                    new EntityVillager.ListItemForEmeralds(ItemHandler.AMBER, new EntityVillager.PriceInfo(-2, -1)),
                    pickedTradeList(1, 2,
                        new EntityVillager.ListItemForEmeralds(Items.BONE, new EntityVillager.PriceInfo(-5, -2)),
                        new EntityVillager.EmeraldForItems(Items.BONE, new EntityVillager.PriceInfo(3, 10))
                    ),
                    pickedTradeList(0, 3,
                        new EntityVillager.ListItemForEmeralds(Items.WOODEN_PICKAXE, new EntityVillager.PriceInfo(3, 6)),
                        new EntityVillager.ListItemForEmeralds(Items.WOODEN_SHOVEL, new EntityVillager.PriceInfo(3, 6)),
                        new EntityVillager.ListItemForEmeralds(Items.STONE_PICKAXE, new EntityVillager.PriceInfo(6, 10)),
                        new EntityVillager.ListItemForEmeralds(Items.STONE_SHOVEL, new EntityVillager.PriceInfo(6, 10))
                    )
                );
                career.addTrade(2,
                    tradeNestedDinosaurVariants(3, 5, 6, 10, ItemHandler.FOSSIL_ITEMS),
                    pickedTradeList(1, 3,
                        ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection().stream().map(d ->
                            new ItemEmeraldFunction(
                                ItemHandler.RAW_MEAT_ITEMS.get(d), ItemHandler.COOKED_MEAT_ITEMS.get(d),
                                new EntityVillager.PriceInfo(1, 5),  new EntityVillager.PriceInfo(6, 10)
                            )
                        ).toArray(EntityVillager.ITradeList[]::new)
                    ),
                    pickedTradeList(0, 3,
                        new EntityVillager.ListItemForEmeralds(Items.IRON_PICKAXE, new EntityVillager.PriceInfo(10, 15)),
                        new EntityVillager.ListItemForEmeralds(Items.IRON_SHOVEL, new EntityVillager.PriceInfo(10, 15))
                    )
                );


                career = new VillagerRegistry.VillagerCareer(profession, "scientist");
                career.addTrade(1,
                    tradeItemMetaVariants(2, 3, 5, 12, 0,
                        ItemHandler.COMPUTER_CHIP_PART, ItemHandler.TANKS_PART, ItemHandler.DRILL_BIT_PART,
                        ItemHandler.LEVELLING_SENSOR_PART, ItemHandler.BULB_PART, ItemHandler.CONTAINER_PART, ItemHandler.TURBINES_PART
                    ),
                    new EntityVillager.ListItemForEmeralds(ItemHandler.TRACKING_TABLET, new EntityVillager.PriceInfo(40, 57)),
                    pickedTradeList(0, 1,
                        new EntityVillager.ListItemForEmeralds(ItemHandler.TRACKING_MODULE, new EntityVillager.PriceInfo(30, 40)),
                        new EntityVillager.ListItemForEmeralds(ItemHandler.FLAPPY_DINO_MODULE, new EntityVillager.PriceInfo(12, 18))
                    )
                );
                career.addTrade(2,
                    tradeDinosaurVariants(1, 2, 20, 32, ItemHandler.TEST_TUBES_GENETIC_MATERIAL),
                    tradeItemMetaVariants(2, 3, 15, 22, 1,
                        ItemHandler.COMPUTER_CHIP_PART, ItemHandler.TANKS_PART, ItemHandler.DRILL_BIT_PART,
                        ItemHandler.BULB_PART, ItemHandler.CONTAINER_PART, ItemHandler.TURBINES_PART
                    )
                );
                career.addTrade(3,
                    tradeItemMetaVariants(1, 2, 25, 32, 2,
                        ItemHandler.COMPUTER_CHIP_PART, ItemHandler.TANKS_PART, ItemHandler.DRILL_BIT_PART, ItemHandler.BULB_PART
                    )
                );
                career.addTrade(4,
                    tradeDinosaurVariants(1, 1, 50, 64, ItemHandler.TEST_TUBES_DNA),
                    tradeItemMetaVariants(1, 1, 35, 42, 3, ItemHandler.TANKS_PART, ItemHandler.DRILL_BIT_PART)
                );


                career = new VillagerRegistry.VillagerCareer(profession, "paleobotany");
                career.addTrade(1,
                    tradeItemMetaVariants(1, 2, -5, -2, 0, Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS),
                    tradeItemMetaVariants(1, 1, -7, -3, EnumDyeColor.WHITE.getMetadata(), Items.DYE),
                    pickedTradeList(1, 1,
                        new EntityVillager.ListItemForEmeralds(Items.WOODEN_HOE, new EntityVillager.PriceInfo(3, 6)),
                        new EntityVillager.ListItemForEmeralds(Items.STONE_HOE, new EntityVillager.PriceInfo(6, 10))
                    )
                );
                career.addTrade(2,
                    pickedTradeList(1, 1,
                        new EntityVillager.ListItemForEmeralds(Items.IRON_HOE, new EntityVillager.PriceInfo(10, 15))
                    )
                );

            })
        );
    }

    //If price is smaller than 0, then it's 1 emerald for -x amount of items;
    //If price is larger than 0, than it's x emeralds for one amount of items.

    public static EntityVillager.ITradeList tradeNestedDinosaurVariants(int min, int max, int priceMin, int priceMax, Map<?, ? extends Map<?, ? extends Item>> map) {
        return tradeListVariants(min, max, priceMin, priceMax, map.values().stream().flatMap(m -> m.values().stream()).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static EntityVillager.ITradeList tradeDinosaurVariants(int min, int max, int priceMin, int priceMax, Map<?, ? extends Item> map) {
        return tradeListVariants(min, max, priceMin, priceMax, map.values().stream().map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static EntityVillager.ITradeList tradeItemMetaVariants(int min, int max, int priceMin, int priceMax, int meta, Item... items) {
        return tradeListVariants(min, max, priceMin, priceMax, Arrays.stream(items).map(item -> new ItemStack(item, 1, item.getMetadata(meta))).toArray(ItemStack[]::new));
    }

    public static EntityVillager.ITradeList tradeListVariants(int min, int max, int priceMin, int priceMax, ItemStack... stacks) {
        EntityVillager.PriceInfo priceInfo = new EntityVillager.PriceInfo(priceMin, priceMax);
        return pickedTradeList(min, max, Arrays.stream(stacks).map(stack -> new EntityVillager.ListItemForEmeralds(stack, priceInfo)).toArray(EntityVillager.ITradeList[]::new));
    }

    public static EntityVillager.ITradeList pickedTradeList(int min, int max, EntityVillager.ITradeList... recipes) {
        List<EntityVillager.ITradeList> possible = new ArrayList<>(Arrays.asList(recipes));
        return (merchant, recipeList, random) -> {
            int amount = min + random.nextInt(max-min);

            List<Integer> ids = IntStream.range(0, possible.size()).boxed().collect(Collectors.toList());
            Collections.shuffle(ids, random);

            List<Integer> chosen = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                chosen.add(ids.get(i));
            }
            Collections.sort(chosen);

            for (int i : chosen) {
                possible.get(i).addMerchantRecipe(merchant, recipeList, random);
            }
        };
    }

    public static VillagerRegistry.VillagerProfession createProfession(String name, Consumer<VillagerRegistry.VillagerProfession> consumer) {
        VillagerRegistry.VillagerProfession profession = new VillagerRegistry.VillagerProfession(
            ProjectNublar.MODID + ":" + name,
            ProjectNublar.MODID + ":textures/entities/villager/" + name + ".png",
            ProjectNublar.MODID + ":textures/entities/villager/" + name + "_zombie.png"
        );
        consumer.accept(profession);
        return profession;
    }

}
