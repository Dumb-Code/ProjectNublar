package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.MachineModuleParts;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.tabs.TabHandler;
import net.dumbcode.projectnublar.server.utils.EnumConnectionType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class BlockHandler {
    public static final SkeletalBuilderBlock SKELETAL_BUILDER = new SkeletalBuilderBlock();
    public static final BlockElectricFencePole LOW_SECURITY_ELECTRIC_FENCE_POLE = new BlockElectricFencePole(EnumConnectionType.LOW_SECURITY);
    public static final BlockElectricFencePole HIGH_SECURITY_ELECTRIC_FENCE_POLE = new BlockElectricFencePole(EnumConnectionType.HIGH_SECURITY);
    public static final BlockElectricFence ELECTRIC_FENCE = new BlockElectricFence();
    public static final BlockCreativePowerSource CREATIVE_POWER_SOURCE = new BlockCreativePowerSource();
    public static final BlockTrackingBeacon TRACKING_BEACON = new BlockTrackingBeacon();
    public static final BlockPylonHead PYLON_HEAD = new BlockPylonHead();

    public static final MachineModuleBlock FOSSIL_PROCESSOR = new MachineModuleBlock(FossilProcessorBlockEntity::new, MachineModuleParts.FOSSIL_PROCESSOR);
    public static final MachineModuleBlock DRILL_EXTRACTOR = new MachineModuleBlock(DrillExtractorBlockEntity::new, MachineModuleParts.DRILL_EXTRACTOR);
    public static final MachineModuleBlock SEQUENCING_SYNTHESIZER = new DyableMachineModuleBlock(SequencingSynthesizerBlockEntity::new, MachineModuleParts.SEQUENCING_SYNTHESIZER,  BlockRenderLayer.CUTOUT);
    public static final MachineModuleBlock EGG_PRINTER = new DyableMachineModuleBlock(EggPrinterBlockEntity::new, MachineModuleParts.EGG_PRINTER, BlockRenderLayer.CUTOUT);
    public static final MachineModuleBlock INCUBATOR = new MachineModuleBlock(IncubatorBlockEntity::new, MachineModuleParts.INCUBATOR);
    public static final MachineModuleBlock COAL_GENERATOR = new MachineModuleBlock(CoalGeneratorBlockEntity::new, MachineModuleParts.COAL_GENERATOR);

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
            PYLON_HEAD.setRegistryName("pylon_head").setTranslationKey("pylon_head").setCreativeTab(TAB)
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

    private static <T extends Block> void populateMap(RegistryEvent.Register<Block> event, Map<Dinosaur, T> itemMap, String dinosaurRegname, Function<Dinosaur, T> supplier) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            T item = supplier.apply(dinosaur);
            String name = String.format(dinosaurRegname, dinosaur.getFormattedName());
            item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
            item.setTranslationKey(name);
            item.setCreativeTab(TAB);
            itemMap.put(dinosaur, item);
            event.getRegistry().register(item);
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
