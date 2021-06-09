package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleParts;
import net.dumbcode.projectnublar.server.tabs.TabHandler;
import net.dumbcode.projectnublar.server.utils.EnumConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Util;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.block.AbstractBlock.Properties.copy;
import static net.minecraft.block.AbstractBlock.Properties.of;

public class BlockHandler {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectNublar.MODID);

    public static final RegistryObject<BlockElectricFencePole> LOW_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("low_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.LOW_SECURITY));
    public static final RegistryObject<BlockElectricFencePole> HIGH_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("high_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.HIGH_SECURITY));
    public static final RegistryObject<BlockElectricFence> ELECTRIC_FENCE = REGISTER.register("electric_fence", () -> new BlockElectricFence(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockCreativePowerSource> CREATIVE_POWER_SOURCE = REGISTER.register("creative_power", () -> new BlockCreativePowerSource(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockTrackingBeacon> TRACKING_BEACON = REGISTER.register("tracking_beacon", () -> new BlockTrackingBeacon(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonHead> PYLON_HEAD = REGISTER.register("pylon_head", () -> new BlockPylonHead(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonPole> PYLON_POLE = REGISTER.register("pylon_pole", () -> new BlockPylonPole(of(Material.HEAVY_METAL)));

    public static final RegistryObject<SkeletalBuilderBlock> SKELETAL_BUILDER = REGISTER.register("skeletal_builder", () -> new SkeletalBuilderBlock(of(Material.HEAVY_METAL).noCollission()));
    public static final RegistryObject<MachineModuleBlock> FOSSIL_PROCESSOR = REGISTER.register("fossil_processor", () -> new MachineModuleBlock(FossilProcessorBlockEntity::new, MachineModuleParts.FOSSIL_PROCESSOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> DRILL_EXTRACTOR = REGISTER.register("drill_extractor", () -> new MachineModuleBlock(DrillExtractorBlockEntity::new, MachineModuleParts.DRILL_EXTRACTOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> SEQUENCING_SYNTHESIZER = REGISTER.register("sequencer_synthesizer", () -> new DyableMachineModuleBlock(SequencingSynthesizerBlockEntity::new, MachineModuleParts.SEQUENCING_SYNTHESIZER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> EGG_PRINTER = REGISTER.register("egg_printer", () -> new DyableMachineModuleBlock(EggPrinterBlockEntity::new, MachineModuleParts.EGG_PRINTER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> INCUBATOR = REGISTER.register("incubator", () -> new MachineModuleBlock(IncubatorBlockEntity::new, MachineModuleParts.INCUBATOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> COAL_GENERATOR = REGISTER.register("coal_generator", () -> new MachineModuleBlock(CoalGeneratorBlockEntity::new, MachineModuleParts.COAL_GENERATOR, of(Material.HEAVY_METAL)));

    public static final RegistryObject<Block> PLANTER_BOX = REGISTER.register("planter_box", () -> new Block(of(Material.METAL)));
    public static final RegistryObject<Block> UNNAMED_SCIENTIST_BLOCK = REGISTER.register("planter_box", () -> new Block(of(Material.METAL)));
    public static final RegistryObject<Block> UNNAMED_PALEONTOLOGIST_BLOCK = REGISTER.register("planter_box", () -> new Block(of(Material.METAL)));

    public static final Map<FossilBlock.FossilType, Map<Dinosaur, RegistryObject<FossilBlock>>> FOSSIL = Util.make(new HashMap<>(), map -> {
        for(FossilBlock.FossilType value : FossilBlock.FossilType.values()) {
            map.put(value, createMap("%s_fossil_" + value.getName(), dinosaur -> new FossilBlock(dinosaur, value, copy(value.getCopy()))));
        }
    });

    private static <T extends Block> Map<Dinosaur, RegistryObject<T>> createMap(String nameFormat, Function<Dinosaur, T> supplier) {
        Map<Dinosaur, RegistryObject<T>> map = new HashMap<>();
        for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
            map.put(dinosaur, REGISTER.register(String.format(nameFormat, dinosaur.getFormattedName()), () -> supplier.apply(dinosaur)));
        }
        return map;
    }
}
