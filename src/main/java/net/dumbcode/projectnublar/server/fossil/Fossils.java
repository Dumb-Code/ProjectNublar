package net.dumbcode.projectnublar.server.fossil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.dumbcode.projectnublar.client.model.fossil.FossilItemRenderer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.fossil.base.Time;
import net.dumbcode.projectnublar.server.fossil.base.serialization.FossilSerializer;
import net.dumbcode.projectnublar.server.fossil.base.serialization.UnSerializedFossilModel;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlock;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
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
            PACK.addAsset(fix(blockName, "models/block", "json"), FossilSerializer.serializeModel(model));
            PACK.addAsset(fix(blockName, "models/item", "json"), FossilSerializer.serializeModel(model));
            PACK.addAsset(fix(blockName, "blockstates", "json"), FossilSerializer.serializeBlockstate("projectnublar:block/" + string));
            PACK.addAsset(new ResourceLocation(ProjectNublar.MODID, "pack.mcmeta"), FossilSerializer.generatePackMcmeta());
            PACK.addLang(new ResourceLocation(ProjectNublar.MODID, "en_us"), JLang.lang().entry(WordUtils.capitalizeFully(stoneType.name), WordUtils.capitalizeFully(fossil.name)));
        }));

        generateFossilBlocks().forEach(((name, block) -> {
            RegistryObject<Block> FOSSIL = FOSSIL_BLOCKS.register(name.replace("_fossil", ""), block);
            BLOCKS.add(FOSSIL);
            RegistryObject<Item> ITEM = ItemHandler.REGISTER.register(name.replace("_fossil", ""), () -> new FossilItem(block.get(), new Item.Properties().tab(ItemHandler.TAB).setISTER(() -> FossilItemRenderer.INSTANCE), ((FossilBlock) block.get()).fossil, ((FossilBlock) block.get()).stone));
            ITEMS.add(ITEM);
        }));
        FossilSerializer.serializeMineableTag(FOSSILS_GENNED);
        FossilSerializer.serializeLang(generateFossilBlocks().keySet());
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

    public static IResourcePack getPack(ResourcePackType type) {
        return PACK;
    }
}
