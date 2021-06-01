package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.MachineModuleParts;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.tabs.TabHandler;
import net.dumbcode.projectnublar.server.utils.EnumConnectionType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static net.minecraft.block.AbstractBlock.Properties.of;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class BlockHandler {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectNublar.MODID);

    public static final RegistryObject<BlockElectricFencePole> LOW_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("low_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.LOW_SECURITY));
    public static final RegistryObject<BlockElectricFencePole> HIGH_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("high_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.HIGH_SECURITY);
    public static final RegistryObject<BlockElectricFence> ELECTRIC_FENCE = REGISTER.register("electric_fence", () -> new BlockElectricFence(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockCreativePowerSource> CREATIVE_POWER_SOURCE = REGISTER.register("creative_power", () -> new BlockCreativePowerSource(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockTrackingBeacon> TRACKING_BEACON = REGISTER.register("tracking_beacon", () -> new BlockTrackingBeacon(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonHead> PYLON_HEAD = REGISTER.register("pylon_head", () -> new BlockPylonHead(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonPole> PYLON_POLE = REGISTER.register("pylon_pole", () -> new BlockPylonPole(of(Material.HEAVY_METAL)));

    public static final RegistryObject<SkeletalBuilderBlock> SKELETAL_BUILDER = REGISTER.register("skeletal_builder", () -> new SkeletalBuilderBlock(of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> FOSSIL_PROCESSOR = REGISTER.register("fossil_processor", () -> new MachineModuleBlock(FossilProcessorBlockEntity::new, MachineModuleParts.FOSSIL_PROCESSOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> DRILL_EXTRACTOR = REGISTER.register("drill_extractor", () -> new MachineModuleBlock(DrillExtractorBlockEntity::new, MachineModuleParts.DRILL_EXTRACTOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> SEQUENCING_SYNTHESIZER = REGISTER.register("sequencer_synthesizer", () -> new DyableMachineModuleBlock(SequencingSynthesizerBlockEntity::new, MachineModuleParts.SEQUENCING_SYNTHESIZER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> EGG_PRINTER = REGISTER.register("egg_printer", () -> new DyableMachineModuleBlock(EggPrinterBlockEntity::new, MachineModuleParts.EGG_PRINTER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> INCUBATOR = REGISTER.register("incubator", () -> new MachineModuleBlock(IncubatorBlockEntity::new, MachineModuleParts.INCUBATOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> COAL_GENERATOR = REGISTER.register("coal_generator", () -> new MachineModuleBlock(CoalGeneratorBlockEntity::new, MachineModuleParts.COAL_GENERATOR, of(Material.HEAVY_METAL)));

    public static final Map<FossilBlock.FossilType, Map<Dinosaur, FossilBlock>> FOSSIL = new HashMap<>();

    private static final CreativeTabs TAB = TabHandler.TAB;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            SKELETAL_BUILDER.setRegistryName("skeletal_builder").setTranslationKey("skeletal_builder").setCreativeTab(TAB),
            FOSSIL_PROCESSOR.setTranslationKey("fossil_processor").setRegistryName("fossil_processor").setCreativeTab(TAB),
            DRILL_EXTRACTOR.setTranslationKey("drill_extractor").setRegistryName("drill_extractor").setCreativeTab(TAB),
            SEQUENCING_SYNTHESIZER.setTranslationKey("sequencer_synthesizer").setRegistryName("sequencer_synthesizer").setCreativeTab(TAB),
            EGG_PRINTER.setTranslationKey("egg_printer").setRegistryName("egg_printer").setCreativeTab(TAB),
            INCUBATOR.setTranslationKey("incubator").setRegistryName("incubator").setCreativeTab(TAB),
            COAL_GENERATOR.setTranslationKey("coal_generator").setRegistryName("coal_generator").setCreativeTab(TAB),
            LOW_SECURITY_ELECTRIC_FENCE_POLE.setRegistryName("low_security_electric_fence_pole").setTranslationKey("low_security_electric_fence_pole").setCreativeTab(TAB),
            HIGH_SECURITY_ELECTRIC_FENCE_POLE.setRegistryName("high_security_electric_fence_pole").setTranslationKey("high_security_electric_fence_pole").setCreativeTab(TAB),
            ELECTRIC_FENCE.setRegistryName("electric_fence").setTranslationKey("electric_fence").setCreativeTab(TAB),
            CREATIVE_POWER_SOURCE.setRegistryName("creative_power").setTranslationKey("creative_power").setCreativeTab(TAB),
            TRACKING_BEACON.setRegistryName("tracking_beacon").setTranslationKey("tracking_beacon").setCreativeTab(TAB),
            PYLON_HEAD.setRegistryName("pylon_head").setTranslationKey("pylon_head").setCreativeTab(TAB),
            PYLON_POLE.setRegistryName("pylon_pole").setTranslationKey("pylon_pole").setHardness(0.2F).setCreativeTab(TAB)
        );

        for (FossilBlock.FossilType value : FossilBlock.FossilType.values()) {
            Map<Dinosaur, FossilBlock> map = Maps.newHashMap();
            FOSSIL.put(value, map);
            populateMap(event, map, "%s_fossil_" + value.getName(), dinosaur -> new FossilBlock(dinosaur, value));
        }

        for (Plant plant : ProjectNublar.PLANT_REGISTRY) {
            event.getRegistry().register(plant.createBlock().setRegistryName(plant.getRegistryName()).setTranslationKey(plant.getRegistryName().getPath()));
        }
    }
    
    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        populateNestedMap(event, itemMap, getterFunction, Object::toString, creationFunc, dinosaurRegname);
    }

    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, Function<S, String> toStringFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            for (S s : getterFunction.apply(dinosaur)) {
                T item = creationFunc.apply(dinosaur, s);
                String name = String.format(dinosaurRegname, dinosaur.getFormattedName(), toStringFunction.apply(s));
                item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                item.setTranslationKey(name);
                item.setCreativeTab(TAB);
                itemMap.computeIfAbsent(dinosaur, d -> new HashMap<>()).put(s, item);
                event.getRegistry().register(item);
            }
        }
    }
}
