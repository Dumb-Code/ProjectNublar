package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.ProjectNublarBlockEntities;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.base.DinosaurAge;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlock;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlockItem;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Fossils {
    public static final List<Fossil> FOSSILS = new ArrayList<>();
    public static final List<RegistryObject<FossilBlock>> BLOCK_REG_OBJECTS = new ArrayList<>();
    private static final List<RegistryObject<Item>> ITEM_REG_OBJECTS = new ArrayList<>();
    public static final Multimap<RegistryObject<Dinosaur>, Block> BLOCKS = Multimaps.newListMultimap(new HashMap<>(), () -> new ArrayList<>());
    public static final Multimap<RegistryObject<Dinosaur>, Item> ITEMS = Multimaps.newListMultimap(new HashMap<>(), () -> new ArrayList<>());
    public static final List<StoneType> STONE_TYPES = new ArrayList<>();
    public static final DeferredRegister<Block> FOSSIL_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectNublar.MODID);

    private static Multimap<StoneType, Fossil> FOSSILS_GENNED;
    public static void generateFossils() {
        generateAllFossilsAndStoneTypes();

        generateFossilBlocks().forEach(((name, block) -> {
            RegistryObject<FossilBlock> FOSSIL = FOSSIL_BLOCKS.register(name.replace("_fossil", ""), block);
            BLOCK_REG_OBJECTS.add(FOSSIL);
            ItemHandler.REGISTER.register(name.replace("_fossil", ""), () -> new FossilBlockItem(block.get(), new Item.Properties().tab(ItemHandler.TAB), ((FossilBlock) block.get()).fossil, ((FossilBlock) block.get()).stone));
        }));
        generateFossilItems().forEach((name, pair) -> {
            ITEM_REG_OBJECTS.add(ItemHandler.REGISTER.register(name, pair.getSecond()));
        });
    }

    public static void generateAllFossilsAndStoneTypes() {
        if (FOSSILS_GENNED != null && !FOSSILS_GENNED.isEmpty()) {
        } else {
            ImmutableMultimap.Builder<StoneType, Fossil> builder = new ImmutableMultimap.Builder<>();
            for (Fossil fossil : FOSSILS) {
                if (fossil.stoneTypes != null) {
                    for (StoneType stoneType : fossil.stoneTypes) {
                        builder.put(stoneType, fossil);
                    }
                } else {
                    List<StoneType> types = DinosaurAge.findAllStoneTypesThatMatchTimePeriods(DinosaurAge.findAllTimePeriodsThatMatchRange(fossil.timeStart, fossil.timeEnd));
                    for (StoneType type : types) {
                        builder.put(type, fossil);
                    }
                }
            }
            FOSSILS_GENNED = builder.build();
        }
    }

    public static Map<String, Supplier<FossilBlock>> generateFossilBlocks() {
        Map<String, Supplier<FossilBlock>> blocks = new HashMap<>();
        generateAllFossilsAndStoneTypes();
        FOSSILS_GENNED.forEach(((stoneType, fossil) -> {
            ResourceLocation blockName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase() + (fossil.appendFossil ? "_fossil": ""));
            FossilBlock block = new FossilBlock(AbstractBlock.Properties.of(Material.STONE).noOcclusion(), fossil, stoneType);
            blocks.put(blockName.getPath(), () -> block);
        }));
        return blocks;
    }

    public static Map<String, Pair<Fossil, Supplier<Item>>> generateFossilItems() {
        Map<String, Pair<Fossil, Supplier<Item>>> blocks = new HashMap<>();
        FOSSILS.forEach(((fossil) -> {
            ResourceLocation itemName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_item");
            FossilItem item = new FossilItem(new Item.Properties().tab(ItemHandler.TAB), fossil);
            blocks.put(itemName.getPath(), new Pair<>(fossil, () -> item));
        }));
        return blocks;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ProjectNublar.MODID)
    public static class Events {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerItem(RegistryEvent.Register<Item> event) {
            List<Item> items = ITEM_REG_OBJECTS.stream().map(RegistryObject::get).collect(Collectors.toList());
            for (Item item : items) {
                ITEMS.put(((FossilItem) item).getFossil().dinosaur, item);
            }
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Pre event) {
            if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
                for (Fossil fossil : Fossils.FOSSILS) {
                    if (fossil.texture != null) {
                        event.addSprite(fossil.texture);
                    }
                    if (fossil.itemTexture != null) {
                        event.addSprite(fossil.itemTexture);
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerBlock(RegistryEvent.Register<Block> event) {
            List<Block> blocks = BLOCK_REG_OBJECTS.stream().map(RegistryObject::get).collect(Collectors.toList());
            for (Block item : blocks) {
                BLOCKS.put(((FossilBlock) item).getFossil().dinosaur, item);
            }
            ProjectNublarBlockEntities.deferredFossilBlockEntityRegister();
        }
    }
}
