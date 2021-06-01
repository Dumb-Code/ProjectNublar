package net.dumbcode.projectnublar.server.block.entity;

import com.mojang.datafixers.types.constant.EmptyPart;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectNublarBlockEntities {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ProjectNublar.MODID);

    public static final RegistryObject<TileEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR
        = REGISTER.register("coal_generator", () -> TileEntityType.Builder.of(CoalGeneratorBlockEntity::new, ).build(new EmptyPart()));



}
