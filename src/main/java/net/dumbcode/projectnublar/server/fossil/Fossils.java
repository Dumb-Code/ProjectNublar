package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.fossil.base.DinosaurAge;
import net.dumbcode.projectnublar.server.fossil.base.serialization.FossilSerializer;
import net.dumbcode.projectnublar.server.fossil.base.serialization.UnSerializedFossilModel;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlock;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlockItem;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl.fix;

public class Fossils {
    public static final RuntimeResourcePack PACK = RuntimeResourcePack.create(new ResourceLocation(ProjectNublar.MODID, "fossil"));
    public static final List<Fossil> FOSSILS = new ArrayList<>();
    public static final List<RegistryObject<Block>> BLOCK_REG_OBJECTS = new ArrayList<>();
    private static final List<RegistryObject<Item>> ITEM_REG_OBJECTS = new ArrayList<>();
    public static final Map<Dinosaur, Block> BLOCKS = new HashMap<>();
    public static final Map<Dinosaur, Item> ITEMS = new HashMap<>();
    public static final List<StoneType> STONE_TYPES = new ArrayList<>();
    public static final DeferredRegister<Block> FOSSIL_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectNublar.MODID);

    private static Multimap<StoneType, Fossil> FOSSILS_GENNED;
    public static void generateFossils() {
        generateAllFossilsAndStoneTypes();
        FOSSILS_GENNED.forEach(((stoneType, fossil) -> {
            UnSerializedFossilModel model = new UnSerializedFossilModel(stoneType, fossil.texture.toString(), stoneType.tint);
            ResourceLocation blockName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase());
            String string = fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase();
            PACK.addAsset(fix(blockName, "models/block", "json"), FossilSerializer.serializeModel(model));
            PACK.addAsset(fix(blockName, "models/item", "json"), FossilSerializer.serializeModel(model));
            PACK.addAsset(fix(blockName, "blockstates", "json"), FossilSerializer.serializeBlockstate("projectnublar:block/" + string));
            PACK.addAsset(new ResourceLocation(ProjectNublar.MODID, "pack.mcmeta"), FossilSerializer.generatePackMcmeta());
        }));

        generateFossilBlocks().forEach(((name, block) -> {
            RegistryObject<Block> FOSSIL = FOSSIL_BLOCKS.register(name.replace("_fossil", ""), block);
            BLOCK_REG_OBJECTS.add(FOSSIL);
            ItemHandler.REGISTER.register(name.replace("_fossil", ""), () -> new FossilBlockItem(block.get(), new Item.Properties().tab(ItemHandler.TAB), ((FossilBlock) block.get()).fossil, ((FossilBlock) block.get()).stone));
        }));
        generateFossilItems().forEach((name, pair) -> {
            ITEM_REG_OBJECTS.add(ItemHandler.REGISTER.register(name + "_item", pair.getSecond()));
            if (pair.getFirst().itemTexture != null) {
                PACK.addAsset(fix(new ResourceLocation(ProjectNublar.MODID, name + "_item"), "models/item", "json"), FossilSerializer.serializeItemModel(pair.getFirst().itemTexture.toString()));
            }
        });
        FossilSerializer.serializeMineableTag(FOSSILS_GENNED);
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

    public static Map<String, Supplier<Block>> generateFossilBlocks() {
        Map<String, Supplier<Block>> blocks = new HashMap<>();
        generateAllFossilsAndStoneTypes();
        FOSSILS_GENNED.forEach(((stoneType, fossil) -> {
            ResourceLocation blockName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase() + (fossil.appendFossil ? "_fossil": ""));
            FossilBlock block = new FossilBlock(AbstractBlock.Properties.of(stoneType.material).noOcclusion().strength(stoneType.strength, stoneType.blastStrength).requiresCorrectToolForDrops().harvestLevel(stoneType.harvestLevel), fossil, stoneType);
            blocks.put(blockName.getPath(), () -> block);
        }));
        return blocks;
    }

    public static Map<String, Pair<Fossil, Supplier<Item>>> generateFossilItems() {
        Map<String, Pair<Fossil, Supplier<Item>>> blocks = new HashMap<>();
        FOSSILS.forEach(((fossil) -> {
            ResourceLocation itemName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + "_item");
            FossilItem item = new FossilItem(new Item.Properties().tab(ItemHandler.TAB), fossil);
            blocks.put(itemName.getPath(), new Pair<>(fossil, () -> item));
        }));
        return blocks;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = ProjectNublar.MODID)
    public static class Events {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerItem(RegistryEvent.Register<Item> event) {
            List<Item> items = ITEM_REG_OBJECTS.stream().map(RegistryObject::get).collect(Collectors.toList());
            for (Item item : items) {
                ITEMS.put(((FossilItem) item).getFossil().dinosaur.get(), item);
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerBlock(RegistryEvent.Register<Block> event) {
            List<Block> blocks = BLOCK_REG_OBJECTS.stream().map(RegistryObject::get).collect(Collectors.toList());
            for (Block item : blocks) {
                BLOCKS.put(((FossilBlock) item).getFossil().dinosaur.get(), item);
            }
        }
    }
}
