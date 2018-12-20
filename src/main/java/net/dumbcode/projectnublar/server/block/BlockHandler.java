package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.MachineModule;
import net.dumbcode.projectnublar.server.utils.ConnectionType;
import net.minecraft.block.Block;
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
    public static final BlockElectricFencePole LIGHT_STEEL_ELECTRIC_FENCE_POLE = new BlockElectricFencePole(ConnectionType.LIGHT_STEEL);
    public static final BlockElectricFencePole HIGH_SECURITY_ELECTRIC_FENCE_POLE = new BlockElectricFencePole(ConnectionType.HIGH_SECURITY);
    public static final BlockElectricFence ELECTRIC_FENCE = new BlockElectricFence();

    public static final MachineModuleBlock FOSSIL_PROCESSOR = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, FossilProcessorBlockEntity::new);
    public static final MachineModuleBlock DRILL_EXTRACTOR = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, DrillExtractorBlockEntity::new);
    public static final MachineModuleBlock SEQUENCING_SYNTHESIZER = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, SequencingSynthesizerBlockEntity::new);
    public static final MachineModuleBlock EGG_PRINTER = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, EggPrinterBlockEntity::new);
    public static final MachineModuleBlock INCUBATOR = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, IncubatorBlockEntity::new);
    public static final MachineModuleBlock COAL_GENERATOR = new MachineModuleBlock<>(MachineModule.TEST_MACHINES, CoalGeneratorBlockEntity::new);

    public static final Map<Dinosaur, FossilBlock> FOSSIlS = new HashMap<>();


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                SKELETAL_BUILDER.setRegistryName("skeletal_builder").setUnlocalizedName("skeletal_builder"),
                FOSSIL_PROCESSOR.setUnlocalizedName("fossil_processor").setRegistryName("fossil_processor"),
                DRILL_EXTRACTOR.setUnlocalizedName("drill_extractor").setRegistryName("drill_extractor"),
                SEQUENCING_SYNTHESIZER.setUnlocalizedName("sequencer_synthesizer").setRegistryName("sequencer_synthesizer"),
                EGG_PRINTER.setUnlocalizedName("egg_printer").setRegistryName("egg_printer"),
                INCUBATOR.setUnlocalizedName("incubator").setRegistryName("incubator"),
                COAL_GENERATOR.setUnlocalizedName("coal_generator").setRegistryName("coal_generator"),
                LIGHT_STEEL_ELECTRIC_FENCE_POLE.setRegistryName("light_steel_electric_fence_pole").setUnlocalizedName("light_steel_electric_fence_pole"),
                HIGH_SECURITY_ELECTRIC_FENCE_POLE.setRegistryName("high_security_electric_fence_pole").setUnlocalizedName("high_security_electric_fence_pole"),
                ELECTRIC_FENCE.setRegistryName("electric_fence").setUnlocalizedName("electric_fence")
        );


        populateMap(event, FOSSIlS, "%s_fossil", FossilBlock::new);
    }

    private static <T extends Block> void populateMap(RegistryEvent.Register<Block> event, Map<Dinosaur, T> itemMap, String dinosaurRegname, Function<Dinosaur, T> supplier) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            if(dinosaur != Dinosaur.MISSING) {
                T item = supplier.apply(dinosaur);
                String name = String.format(dinosaurRegname, dinosaur.getFormattedName());
                item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                item.setUnlocalizedName(name);
                item.setCreativeTab(ProjectNublar.TAB);
                itemMap.put(dinosaur, item);
                event.getRegistry().register(item);
            }
        }
    }

    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        populateNestedMap(event, itemMap, getterFunction, Object::toString, creationFunc, dinosaurRegname);
    }

    private static <T extends Block, S> void populateNestedMap(RegistryEvent.Register<Block> event, Map<Dinosaur, Map<S, T>> itemMap, Function<Dinosaur, Collection<S>> getterFunction, Function<S, String> toStringFunction, BiFunction<Dinosaur, S, T> creationFunc, String dinosaurRegname) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY) {
            if(dinosaur != Dinosaur.MISSING) {
                for (S s : getterFunction.apply(dinosaur)) {
                    T item = creationFunc.apply(dinosaur, s);
                    String name = String.format(dinosaurRegname, dinosaur.getFormattedName(), toStringFunction.apply(s));
                    item.setRegistryName(new ResourceLocation(ProjectNublar.MODID, name));
                    item.setUnlocalizedName(name);
                    item.setCreativeTab(ProjectNublar.TAB);
                    itemMap.computeIfAbsent(dinosaur, d -> new HashMap<>()).put(s, item);
                    event.getRegistry().register(item);
                }
            }
        }
    }

//    @Nonnull//TODO: fix
    @SuppressWarnings("all")
    private static <T> T getNonNull() { //Used to prevent compiler warnings on object holders
        return null;
    }
}
