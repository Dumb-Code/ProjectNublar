package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class StoneTypeHandler {
    public static final PostEarlyDeferredRegister<StoneType> DR = PostEarlyDeferredRegister.create(StoneType.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<StoneType>> STONE_TYPE_REGISTRY = DR.makeRegistry("stone_type", () -> new RegistryBuilder<StoneType>()
            .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "granite")));

    public static RegistryObject<StoneType> GRANITE = DR.register("granite", () -> new StoneType(1000, 0, Blocks.GRANITE::defaultBlockState, 50));
    public static RegistryObject<StoneType> DIORITE = DR.register("diorite", () -> new StoneType(1000, 0, Blocks.DIORITE::defaultBlockState, 50));
    public static RegistryObject<StoneType> COBBLESTONE = DR.register("cobblestone", () -> new StoneType(1000, 0, Blocks.COBBLESTONE::defaultBlockState, 50));
    public static RegistryObject<StoneType> CLAY = DR.register("clay", () -> new StoneType(1000, 0, Blocks.CLAY::defaultBlockState, 50));
    public static RegistryObject<StoneType> TERRACOTTA = DR.register("terracotta", () -> new StoneType(1000, 0, Blocks.TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> WHITE_TERRACOTTA = DR.register("white_terracotta", () -> new StoneType(1000, 0, Blocks.WHITE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> ORANGE_TERRACOTTA = DR.register("orange_terracotta", () -> new StoneType(1000, 0, Blocks.ORANGE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> MAGENTA_TERRACOTTA = DR.register("magenta_terracotta", () -> new StoneType(1000, 0, Blocks.MAGENTA_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIGHT_BLUE_TERRACOTTA = DR.register("light_blue_terracotta", () -> new StoneType(1000, 0, Blocks.LIGHT_BLUE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> YELLOW_TERRACOTTA = DR.register("yellow_terracotta", () -> new StoneType(1000, 0, Blocks.YELLOW_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIME_TERRACOTTA = DR.register("lime_terracotta", () -> new StoneType(1000, 0, Blocks.LIME_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> PINK_TERRACOTTA = DR.register("pink_terracotta", () -> new StoneType(1000, 0, Blocks.PINK_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> GRAY_TERRACOTTA = DR.register("gray_terracotta", () -> new StoneType(1000, 0, Blocks.GRAY_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIGHT_GRAY_TERRACOTTA = DR.register("light_gray_terracotta", () -> new StoneType(1000, 0, Blocks.LIGHT_GRAY_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> CYAN_TERRACOTTA = DR.register("cyan_terracotta", () -> new StoneType(1000, 0, Blocks.CYAN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> PURPLE_TERRACOTTA = DR.register("purple_terracotta", () -> new StoneType(1000, 0, Blocks.PURPLE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> BROWN_TERRACOTTA = DR.register("brown_terracotta", () -> new StoneType(1000, 0, Blocks.BROWN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> GREEN_TERRACOTTA = DR.register("green_terracotta", () -> new StoneType(1000, 0, Blocks.GREEN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> RED_TERRACOTTA = DR.register("red_terracotta", () -> new StoneType(1000, 0, Blocks.RED_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> BLACK_TERRACOTTA = DR.register("black_terracotta", () -> new StoneType(1000, 0, Blocks.BLACK_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> NETHERRACK = DR.register("netherrack", () -> new StoneType(1000, 0, Blocks.NETHERRACK::defaultBlockState, 50));
    public static RegistryObject<StoneType> BLACKSTONE = DR.register("blackstone", () -> new StoneType(1000, 0, Blocks.BLACKSTONE::defaultBlockState, 50));
}
