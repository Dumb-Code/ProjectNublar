package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dumbcode.projectnublar.client.model.fossil.FossilItemRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.lang.JLang;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.function.Supplier;

import static net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl.fix;

public class Fossils {
    public static final RuntimeResourcePack PACK = RuntimeResourcePack.create(new ResourceLocation(ProjectNublar.MODID, "fossil"));
    public static final List<Fossil> FOSSILS = new ArrayList<>();
    public static final List<RegistryObject<Block>> BLOCKS = new ArrayList<>();
    public static final List<RegistryObject<Item>> ITEMS = new ArrayList<>();
    public static final List<StoneType> STONE_TYPES = new ArrayList<>();
    public static final DeferredRegister<Block> FOSSIL_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectNublar.MODID);

    private static Multimap<StoneType, Fossil> FOSSILS_GENNED;
    public static void generateFossils() {
        generateAllFossilsAndStoneTypes();
        FOSSILS_GENNED.forEach(((stoneType, fossil) -> {
            UnSerializedFossilModel model = new UnSerializedFossilModel(stoneType.texture.toString(), fossil.texture.toString(), stoneType.tint);
            ResourceLocation blockName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase());
            String string = fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase();
            PACK.addAsset(fix(blockName, "models/block", "json"), FossilSerializer.serialize(model));
            PACK.addAsset(fix(blockName, "models/item", "json"), FossilSerializer.serialize(model));
            PACK.addAsset(fix(blockName, "blockstates", "json"), FossilSerializer.serialize("projectnublar:block/" + string));
            PACK.addAsset(new ResourceLocation(ProjectNublar.MODID, "pack.mcmeta"), FossilSerializer.serialize());
            PACK.addLang(new ResourceLocation(ProjectNublar.MODID, "en_us"), JLang.lang().entry(WordUtils.capitalizeFully(stoneType.name), WordUtils.capitalizeFully(fossil.name)));
        }));

        generateFossilBlocks().forEach(((name, block) -> {
            RegistryObject<Block> FOSSIL = FOSSIL_BLOCKS.register(name, block);
            BLOCKS.add(FOSSIL);
            RegistryObject<Item> ITEM = ItemHandler.REGISTER.register(name, () -> new FossilItem(block.get(), new Item.Properties().tab(ItemHandler.TAB).setISTER(() -> FossilItemRenderer.INSTANCE), ((FossilBlock) block.get()).fossil, ((FossilBlock) block.get()).stone));
            ITEMS.add(ITEM);
        }));
        FossilSerializer.serialize(FOSSILS_GENNED);
    }

    //TODO: remove as this is for testing only
    public static void addBuiltInFossilsAndTypes() {
        FOSSILS.add(new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/ammonite"), "Ammonite"));
        FOSSILS.add(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/feather"), "Feather"));
        FOSSILS.add(new Fossil(365, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/feet"), "Feet"));
        FOSSILS.add(new Fossil(530, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fish"), "Fish"));
        FOSSILS.add(new Fossil(275, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/imprint"), "Imprint"));
        FOSSILS.add(new Fossil(260, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/lizard"), "Lizard"));
        FOSSILS.add(new Fossil(500, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/tooth"), "Tooth"));
        FOSSILS.add(new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/trilobite"), "Trilobite"));

        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/stone"), "Stone", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,158, 120, 104), new ResourceLocation("block/granite"), "Granite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/andesite"), "Andesite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/diorite"), "Diorite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/clay"), "Stone", "mineable/pickaxe", Material.STONE, 0, 0.6F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,158, 120, 104), new ResourceLocation("block/terracotta"), "Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/white_terracotta"), "White Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/orange_terracotta"), "Orange Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/magenta_terracotta"), "Magenta Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,158, 120, 104), new ResourceLocation("block/light_blue_terracotta"), "Light Blue Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/yellow_terracotta"), "Yellow Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/lime_terracotta"), "Lime Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/pink_terracotta"), "Pink Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,158, 120, 104), new ResourceLocation("block/gray_terracotta"), "Gray Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/light_gray_terracotta"), "Light Gray Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/cyan_terracotta"), "Cyan Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/purple_terracotta"), "Purple Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/blue_terracotta"), "Blue Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/brown_terracotta"), "Brown Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,158, 120, 104), new ResourceLocation("block/green_terracotta"), "Green Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,145, 145, 145), new ResourceLocation("block/red_terracotta"), "Red Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        STONE_TYPES.add(new StoneType(1000, 0, color(255,179, 179, 179), new ResourceLocation("block/black_terracotta"), "Black Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
//        STONE_TYPES.add(new StoneType(1000, 0, color(255,184, 171, 136), new ResourceLocation("block/sandstone"), "Sandstone", "mineable/pickaxe", Material.STONE, 0, 0.8F));
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
                    List<StoneType> types = Time.findAllStoneTypesThatMatchTimePeriods(Time.findAllTimePeriodsThatMatchRange(fossil.timeStart, fossil.timeEnd));
                    for (StoneType type : types) {
                        builder.put(type, fossil);
                    }
                }
            }
            FOSSILS_GENNED = builder.build();
        }
    }

    public static int alpha(int pPackedColor) {
        return pPackedColor >>> 24;
    }

    public static int red(int pPackedColor) {
        return pPackedColor >> 16 & 255;
    }

    public static int green(int pPackedColor) {
        return pPackedColor >> 8 & 255;
    }

    public static int blue(int pPackedColor) {
        return pPackedColor & 255;
    }

    public static int color(int pAlpha, int pRed, int pGreen, int pBlue) {
        return pAlpha << 24 | pRed << 16 | pGreen << 8 | pBlue;
    }

    //TODO
    public static Map<String, Supplier<Block>> generateFossilBlocks() {
        Map<String, Supplier<Block>> blocks = new HashMap<>();
        generateAllFossilsAndStoneTypes();
        FOSSILS_GENNED.forEach(((stoneType, fossil) -> {
            ResourceLocation blockName = new ResourceLocation(ProjectNublar.MODID, fossil.name.replace(" ", "_").toLowerCase() + "_" + stoneType.name.replace(" ", "_").toLowerCase());
            FossilBlock block = new FossilBlock(AbstractBlock.Properties.of(stoneType.material).noOcclusion().strength(stoneType.strength, stoneType.blastStrength).requiresCorrectToolForDrops().harvestLevel(stoneType.harvestLevel), fossil, stoneType);
            blocks.put(blockName.getPath(), () -> block);
        }));
        return blocks;
    }

    public static IResourcePack getPack(ResourcePackType type) {
        return PACK;
    }
}
