package net.dumbcode.projectnublar.server.villager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.fossil.Fossils;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.SoundEvents;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.dumbcode.projectnublar.server.block.BlockHandler.UNNAMED_PALEONTOLOGIST_BLOCK;
import static net.dumbcode.projectnublar.server.block.BlockHandler.UNNAMED_SCIENTIST_BLOCK;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class VillagerHandler {

    public static final DeferredRegister<PointOfInterestType> POI_REGISTER = DeferredRegister.create(ForgeRegistries.POI_TYPES, ProjectNublar.MODID);
    public static final RegistryObject<PointOfInterestType> PLANTER_BOX = POI_REGISTER.register("planter_box", () -> new PointOfInterestType("planter_box", ImmutableSet.of(BlockHandler.PLANTER_BOX.get().defaultBlockState()), 1, 1));
    public static final RegistryObject<PointOfInterestType> UNNAMED_SCIENTIST = POI_REGISTER.register("unnamed_scientist", () -> new PointOfInterestType("unnamed_scientist", ImmutableSet.of(UNNAMED_SCIENTIST_BLOCK.get().defaultBlockState()), 1, 1));
    public static final RegistryObject<PointOfInterestType> UNNAMED_PALEONTOLOGIST = POI_REGISTER.register("unnamed_paleontolist", () -> new PointOfInterestType("unnamed_paleontologist", ImmutableSet.of(UNNAMED_PALEONTOLOGIST_BLOCK.get().defaultBlockState()), 1, 1));

    public static final DeferredRegister<VillagerProfession> PROFESSION_REGISTER = DeferredRegister.create(ForgeRegistries.PROFESSIONS, ProjectNublar.MODID);
    public static final RegistryObject<VillagerProfession> PALEONTOLOGIST = PROFESSION_REGISTER.register("paleontologist", () -> new VillagerProfession("paleontologist", UNNAMED_PALEONTOLOGIST.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LEATHERWORKER));
    public static final RegistryObject<VillagerProfession> SCIENTIST = PROFESSION_REGISTER.register("scientist", () -> new VillagerProfession("scientist", UNNAMED_SCIENTIST.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LEATHERWORKER));
    public static final RegistryObject<VillagerProfession> PALEOBOTANIST = PROFESSION_REGISTER.register("paleobotanist", () -> new VillagerProfession("paleobotanist", PLANTER_BOX.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LEATHERWORKER));

    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() == PALEONTOLOGIST.get()) {
            event.getTrades().putAll(new ImmutableMap.Builder<Integer, List<VillagerTrades.ITrade>>()
                .put(1, Arrays.asList(
                    new BasicTrade(2, new ItemStack(ItemHandler.AMBER.get()), 16, 1, 2F),
                    new BasicTrade(3, new ItemStack(Items.BONE), 1, 1, 0.05F),

                    new BasicTrade(5, new ItemStack(Items.WOODEN_PICKAXE), 4, 1, 0.5F),
                    new BasicTrade(5, new ItemStack(Items.WOODEN_SHOVEL), 4, 1, 0.5F),
                    new BasicTrade(7, new ItemStack(Items.STONE_PICKAXE), 4, 2, 0.5F),
                    new BasicTrade(7, new ItemStack(Items.STONE_SHOVEL), 4, 2, 0.5F)
                ))
                .put(2, Arrays.asList(
                    RandomChoiceCheckTrade.ofMap(4, 7, 8, 2, 0.5F, Fossils.ITEMS),
                    new UseEmeraldToCookRawMeat(3, 6, 16, 2, 0.5F),
                    new BasicTrade(11, new ItemStack(Items.IRON_PICKAXE), 4, 1, 0.5F),
                    new BasicTrade(11, new ItemStack(Items.IRON_SHOVEL), 4, 1, 0.5F)
                ))
                .build()
            );
        }

        if (event.getType() == SCIENTIST.get()) {
            event.getTrades().putAll(new ImmutableMap.Builder<Integer, List<VillagerTrades.ITrade>>()
                .put(1, Arrays.asList(
                    new RandomChoiceCheckTrade(10, 1, 16, 2, 0.5F,
                        new ItemStack(ItemHandler.COMPUTER_CHIP_PART_1.get()), new ItemStack(ItemHandler.TANKS_PART_1.get()),
                        new ItemStack(ItemHandler.DRILL_BIT_PART_1.get()), new ItemStack(ItemHandler.LEVELLING_SENSOR_PART.get()),
                        new ItemStack(ItemHandler.BULB_PART_1.get()), new ItemStack(ItemHandler.CONTAINER_PART_1.get()),
                        new ItemStack(ItemHandler.TURBINES_PART_1.get())
                    ),
                    new RandomChoiceCheckTrade(10, 1, 16, 2, 0.5F,
                        new ItemStack(ItemHandler.COMPUTER_CHIP_PART_1.get()), new ItemStack(ItemHandler.TANKS_PART_1.get()),
                        new ItemStack(ItemHandler.DRILL_BIT_PART_1.get()), new ItemStack(ItemHandler.LEVELLING_SENSOR_PART.get()),
                        new ItemStack(ItemHandler.BULB_PART_1.get()), new ItemStack(ItemHandler.CONTAINER_PART_1.get()),
                        new ItemStack(ItemHandler.TURBINES_PART_1.get())
                    ),
                    new BasicTrade(40, new ItemStack(ItemHandler.TABLET.get()), 1, 10, 0.5F),
                    new RandomTrade(
                        new BasicTrade(30, new ItemStack(ItemHandler.TRACKING_MODULE.get()), 2, 7, 0.3F),
                        new BasicTrade(12, new ItemStack(ItemHandler.TRACKING_MODULE.get()), 2, 5, 0.3F)
                    )
                ))
                .put(2, Arrays.asList(
                    RandomChoiceCheckTrade.ofMap(20, 1, 8, 6, 1.5F, ItemHandler.TEST_TUBES_GENETIC_MATERIAL),
                    new RandomChoiceCheckTrade(15, 1, 8, 5, 0.75F,
                        new ItemStack(ItemHandler.COMPUTER_CHIP_PART_2.get()), new ItemStack(ItemHandler.TANKS_PART_2.get()),
                        new ItemStack(ItemHandler.DRILL_BIT_PART_2.get()), new ItemStack(ItemHandler.BULB_PART_2.get()),
                        new ItemStack(ItemHandler.TURBINES_PART_2.get())
                    )
                ))
                .put(3, Arrays.asList(
                    new RandomChoiceCheckTrade(30, 1, 4, 8, 1F,
                        new ItemStack(ItemHandler.COMPUTER_CHIP_PART_3.get()), new ItemStack(ItemHandler.TANKS_PART_3.get()),
                        new ItemStack(ItemHandler.DRILL_BIT_PART_3.get()), new ItemStack(ItemHandler.BULB_PART_3.get()),
                        new ItemStack(ItemHandler.CONTAINER_PART_2.get())
                    )
                ))
                .put(4, Arrays.asList(
                    RandomChoiceCheckTrade.ofMap(30, 1, 2, 15, 1.5F, ItemHandler.TEST_TUBES_DNA),
                    new RandomChoiceCheckTrade(25, 1, 2, 12, 1.25F,
                        new ItemStack(ItemHandler.TANKS_PART_4.get()), new ItemStack(ItemHandler.DRILL_BIT_PART_4.get())
                    )
                ))
                .build()
            );
        }

        if (event.getType() == PALEOBOTANIST.get()) {
            event.getTrades().putAll(new ImmutableMap.Builder<Integer, List<VillagerTrades.ITrade>>()
                .put(1, Arrays.asList(
                    new RandomChoiceCheckTrade(1, 5, 16, 1, 0.05F,
                        new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.PUMPKIN_SEEDS),
                        new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.PUMPKIN_SEEDS)
                    ),
                    new BasicTrade(1, new ItemStack(Items.BONE_MEAL, 5), 16, 1, 0.05F),
                    new RandomTrade(
                        new BasicTrade(5, new ItemStack(Items.WOODEN_HOE), 16, 1),
                        new BasicTrade(7, new ItemStack(Items.STONE_HOE), 16, 1)
                    )
                ))
                .put(2, Arrays.asList(
                    new BasicTrade(11, new ItemStack(Items.IRON_HOE), 8, 2)
                ))
                .build()
            );
        }
    }

    public static class RandomTrade implements VillagerTrades.ITrade {

        private final VillagerTrades.ITrade[] trades;

        public RandomTrade(VillagerTrades.ITrade... trades) {
            this.trades = trades;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {
            return this.trades[random.nextInt(this.trades.length)].getOffer(entity, random);
        }
    }

    public static class RandomChoiceCheckTrade implements VillagerTrades.ITrade {

        private final int emeralds;
        private final int result;
        private final int maxTrades;
        private final int xp;
        private final float mul;
        private final ItemStack[] stacks;

        public RandomChoiceCheckTrade(int emeralds, int result, int maxTrades, int xp, float mul, ItemStack... stacks) {
            this.stacks = stacks;
            this.emeralds = emeralds;
            this.result = result;
            this.maxTrades = maxTrades;
            this.xp = xp;
            this.mul = mul;
        }

        public static RandomChoiceCheckTrade ofMap(int emeralds, int result, int maxTrades, int xp, float mul, Map<?, Item> map) {
            return new RandomChoiceCheckTrade(emeralds, result, maxTrades, xp, mul, map.values().stream()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new)
            );
        }

        public static RandomChoiceCheckTrade ofMap(int emeralds, int result, int maxTrades, int xp, float mul, RegistryMap<?, Item> map) {
            return new RandomChoiceCheckTrade(emeralds, result, maxTrades, xp, mul, map.values().stream()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new)
            );
        }
        public static RandomChoiceCheckTrade ofNestedMap(int emeralds, int result, int maxTrades, int xp, float mul, Map<?, ? extends RegistryMap<?, Item>> nestedMap) {
            return new RandomChoiceCheckTrade(emeralds, result, maxTrades, xp, mul, nestedMap.values().stream()
                .flatMap(m -> m.values().stream())
                .map(ItemStack::new)
                .toArray(ItemStack[]::new)
            );
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {
            ItemStack out = this.stacks[random.nextInt(this.stacks.length)].copy();
            out.setCount(this.result);
            return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeralds), out, this.maxTrades, this.xp, this.mul);
        }
    }

}
