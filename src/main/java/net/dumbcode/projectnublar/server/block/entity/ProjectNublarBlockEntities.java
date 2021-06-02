package net.dumbcode.projectnublar.server.block.entity;

import com.mojang.datafixers.types.constant.EmptyPart;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectNublarBlockEntities {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ProjectNublar.MODID);

    public static final RegistryObject<TileEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR
        = REGISTER.register("coal_generator", () -> TileEntityType.Builder.of(CoalGeneratorBlockEntity::new, BlockHandler.COAL_GENERATOR.get()).build(new EmptyPart()));

    public static final RegistryObject<TileEntityType<BlockEntityElectricFence>> ELECTRIC_FENCE
        = REGISTER.register("electric_fence", () -> TileEntityType.Builder.of(BlockEntityElectricFence::new, BlockHandler.ELECTRIC_FENCE.get()).build(new EmptyPart()));
    public static final RegistryObject<TileEntityType<BlockEntityElectricFencePole>> ELECTRIC_FENCE_POLE
        = REGISTER.register("electric_fence_pole", () -> TileEntityType.Builder.of(BlockEntityElectricFencePole::new, BlockHandler.HIGH_SECURITY_ELECTRIC_FENCE_POLE.get(), BlockHandler.LOW_SECURITY_ELECTRIC_FENCE_POLE.get()).build(new EmptyPart()));


}
