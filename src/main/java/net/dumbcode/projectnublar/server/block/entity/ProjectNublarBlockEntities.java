package net.dumbcode.projectnublar.server.block.entity;

import com.mojang.datafixers.types.constant.EmptyPart;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockCreativePowerSource;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectNublarBlockEntities {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ProjectNublar.MODID);

    public static final RegistryObject<TileEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR
        = REGISTER.register("coal_generator", () -> TileEntityType.Builder.of(CoalGeneratorBlockEntity::new, BlockHandler.COAL_GENERATOR.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<DrillExtractorBlockEntity>> DRILL_EXTRACTOR
        = REGISTER.register("drill_extractor", () -> TileEntityType.Builder.of(DrillExtractorBlockEntity::new, BlockHandler.DRILL_EXTRACTOR.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<EggPrinterBlockEntity>> EGG_PRINTER
        = REGISTER.register("egg_printer", () -> TileEntityType.Builder.of(EggPrinterBlockEntity::new, BlockHandler.EGG_PRINTER.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<FossilProcessorBlockEntity>> FOSSIL_PROCESSOR
        = REGISTER.register("fossil_processor", () -> TileEntityType.Builder.of(FossilProcessorBlockEntity::new, BlockHandler.FOSSIL_PROCESSOR.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<IncubatorBlockEntity>> INCUBATOR
        = REGISTER.register("incubator", () -> TileEntityType.Builder.of(IncubatorBlockEntity::new, BlockHandler.INCUBATOR.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<SequencingSynthesizerBlockEntity>> SEQUENCING_SYNTHESIZER
        = REGISTER.register("sequencing_synthesizer", () -> TileEntityType.Builder.of(SequencingSynthesizerBlockEntity::new, BlockHandler.SEQUENCING_SYNTHESIZER.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<SkeletalBuilderBlockEntity>> SKELETAL_BUILDER
        = REGISTER.register("skeletal_builder", () -> TileEntityType.Builder.of(SkeletalBuilderBlockEntity::new, BlockHandler.SKELETAL_BUILDER.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<TrackingBeaconBlockEntity>> TRACKING_BEACON
        = REGISTER.register("tracking_beacon", () -> TileEntityType.Builder.of(TrackingBeaconBlockEntity::new, BlockHandler.TRACKING_BEACON.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<PylonHeadBlockEntity>> PYLON_HEAD
        = REGISTER.register("pylon_head", () -> TileEntityType.Builder.of(PylonHeadBlockEntity::new, BlockHandler.PYLON_HEAD.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<BlockCreativePowerSource.BlockEntity>> CREATIVE_ENERGY
        = REGISTER.register("creative_energy", () -> TileEntityType.Builder.of(BlockCreativePowerSource.BlockEntity::new, BlockHandler.CREATIVE_POWER_SOURCE.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<BlockEntityElectricFence>> ELECTRIC_FENCE
        = REGISTER.register("electric_fence", () -> TileEntityType.Builder.of(BlockEntityElectricFence::new, BlockHandler.ELECTRIC_FENCE.get()).build(new EmptyPart()));

    public static RegistryObject<TileEntityType<FossilBlockEntity>> FOSSIL = REGISTER.register("fossil", () -> TileEntityType.Builder.of(FossilBlockEntity::new, BlockHandler.FOSSIL_BLOCK.get()).build(new EmptyPart()));;
    public static final RegistryObject<TileEntityType<BlockEntityElectricFencePole>> ELECTRIC_FENCE_POLE
        = REGISTER.register("electric_fence_pole", () -> TileEntityType.Builder.of(BlockEntityElectricFencePole::new, BlockHandler.HIGH_SECURITY_ELECTRIC_FENCE_POLE.get(), BlockHandler.LOW_SECURITY_ELECTRIC_FENCE_POLE.get()).build(new EmptyPart()));

}
