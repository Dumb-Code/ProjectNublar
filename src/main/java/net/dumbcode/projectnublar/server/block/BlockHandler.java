package net.dumbcode.projectnublar.server.block;

import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.*;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlock;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleBuildPart;
import net.dumbcode.projectnublar.server.item.MachineModuleParts;
import net.dumbcode.dumblibrary.server.registry.PreprocessRegisterDeferredRegister;
import net.dumbcode.projectnublar.server.utils.EnumConnectionType;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.block.AbstractBlock.Properties.copy;
import static net.minecraft.block.AbstractBlock.Properties.of;

public class BlockHandler {

    public static final PostEarlyDeferredRegister<Block> REGISTER = PostEarlyDeferredRegister.create(ForgeRegistries.BLOCKS.getRegistrySuperType(), ProjectNublar.MODID);

    public static final RegistryObject<BlockElectricFencePole> LOW_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("low_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.LOW_SECURITY));
    public static final RegistryObject<BlockElectricFencePole> HIGH_SECURITY_ELECTRIC_FENCE_POLE = REGISTER.register("high_security_electric_fence_pole", () -> new BlockElectricFencePole(of(Material.HEAVY_METAL), EnumConnectionType.HIGH_SECURITY));
    public static final RegistryObject<BlockElectricFence> ELECTRIC_FENCE = REGISTER.register("electric_fence", () -> new BlockElectricFence(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockCreativePowerSource> CREATIVE_POWER_SOURCE = REGISTER.register("creative_power", () -> new BlockCreativePowerSource(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockTrackingBeacon> TRACKING_BEACON = REGISTER.register("tracking_beacon", () -> new BlockTrackingBeacon(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonHead> PYLON_HEAD = REGISTER.register("pylon_head", () -> new BlockPylonHead(of(Material.HEAVY_METAL)));
    public static final RegistryObject<BlockPylonPole> PYLON_POLE = REGISTER.register("pylon_pole", () -> new BlockPylonPole(of(Material.HEAVY_METAL)));

    public static final RegistryObject<SkeletalBuilderBlock> SKELETAL_BUILDER = REGISTER.register("skeletal_builder", () -> new SkeletalBuilderBlock(of(Material.HEAVY_METAL).noCollission()));

    public static final RegistryObject<MachineModuleBlock> FOSSIL_PROCESSOR = REGISTER.register("fossil_processor", () -> new MachineModuleBlock(FossilProcessorBlockEntity::new, 3, MachineModuleParts.FOSSIL_PROCESSOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> DRILL_EXTRACTOR = REGISTER.register("drill_extractor", () -> new MachineModuleBlock(DrillExtractorBlockEntity::new, 3, MachineModuleParts.DRILL_EXTRACTOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> SEQUENCING_SYNTHESIZER = REGISTER.register("sequencer_synthesizer", () -> new DyableMachineModuleBlock(SequencingSynthesizerBlockEntity::new, 3, MachineModuleParts.SEQUENCING_SYNTHESIZER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> EGG_PRINTER = REGISTER.register("egg_printer", () -> new DyableMachineModuleBlock(EggPrinterBlockEntity::new, 1, MachineModuleParts.EGG_PRINTER, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> INCUBATOR = REGISTER.register("incubator", () -> new MachineModuleBlock(IncubatorBlockEntity::new, 1, MachineModuleParts.INCUBATOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<MachineModuleBlock> COAL_GENERATOR = REGISTER.register("coal_generator", () -> new MachineModuleBlock(CoalGeneratorBlockEntity::new, 1, MachineModuleParts.COAL_GENERATOR, of(Material.HEAVY_METAL)));
    public static final RegistryObject<FossilBlock> FOSSIL_BLOCK = REGISTER.register("fossil_block", () -> new FossilBlock(of(Material.STONE)));


    public static final RegistryObject<UnbuiltMachineModuleBlock> UNBUILT_FOSSIL_PROCESSOR = REGISTER.register("unbuilt_fossil_processor", () -> new UnbuiltMachineModuleBlock(FOSSIL_PROCESSOR, of(Material.HEAVY_METAL),
        create("body", ItemHandler.FOSSIL_PROCESSOR_BODY),
        create("lid", ItemHandler.FOSSIL_PROCESSOR_LID, "body"),
        create("tanks", ItemHandler.FOSSIL_PROCESSOR_TANKS, "body")
    ));
    public static final RegistryObject<UnbuiltMachineModuleBlock> UNBUILT_SEQUENCING_SYNTHESIZER = REGISTER.register("unbuilt_sequencing_synthesizer", () -> new UnbuiltMachineModuleBlock(SEQUENCING_SYNTHESIZER, of(Material.HEAVY_METAL),
        create("door", ItemHandler.SEQUENCER_DOOR),
        create("computer", ItemHandler.SEQUENCER_COMPUTER),
        create("monitor", ItemHandler.MONITOR, "computer")
    ));
    public static final RegistryObject<UnbuiltMachineModuleBlock> UNBUILT_EGG_PRINTER = REGISTER.register("unbuilt_egg_printer", () -> new UnbuiltMachineModuleBlock(EGG_PRINTER, of(Material.HEAVY_METAL),
        create("lid", ItemHandler.EGG_PRINTER_LID),
        create("platform", ItemHandler.EGG_PRINTER_PLATFORM),
        create("needle", ItemHandler.EGG_PRINTER_NEEDLE)
    ));
    public static final RegistryObject<UnbuiltMachineModuleBlock> UNBUILT_INCUBATOR = REGISTER.register("unbuilt_incubator", () -> new UnbuiltMachineModuleBlock(INCUBATOR, of(Material.HEAVY_METAL),
        create("lid", ItemHandler.INCUBATOR_LID),
        create("nest", ItemHandler.INCUBATOR_NEST),
        create("lift", ItemHandler.INCUBATOR_LIFT),
        create("arm_base", ItemHandler.INCUBATOR_ARM_BASE),
        create("arm", ItemHandler.INCUBATOR_ARM, "arm_base")
    ));


    public static final RegistryObject<Block> PLANTER_BOX = REGISTER.register("planter_box", () -> new Block(of(Material.METAL)));
    public static final RegistryObject<Block> UNNAMED_SCIENTIST_BLOCK = REGISTER.register("unnamed_scientist_block", () -> new Block(of(Material.METAL)));
    public static final RegistryObject<Block> UNNAMED_PALEONTOLOGIST_BLOCK = REGISTER.register("unnamed_paleontologist_block", () -> new Block(of(Material.METAL)));

    private static <T extends Block> RegistryMap<Dinosaur, T> createMap(String nameFormat, Function<Dinosaur, T> supplier) {
        RegistryMap<Dinosaur, T> map = new RegistryMap<>();
        for (Dinosaur dinosaur : DinosaurHandler.getRegistry()) {
            map.putRegistry(dinosaur, REGISTER.register(String.format(nameFormat, dinosaur.getFormattedName()), () -> supplier.apply(dinosaur)));
        }
        return map;
    }

    private static MachineModuleBuildPart create(String name, Supplier<Item> item, String... dependencies) {
        return new MachineModuleBuildPart(name, item, dependencies);
    }
}
