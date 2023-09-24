package net.dumbcode.projectnublar.server.fossil;

import net.dumbcode.dumblibrary.server.registry.PostEarlyDeferredRegister;
import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class StoneTypeHandler {

    public static final PostEarlyDeferredRegister<StoneType> REGISTER = PostEarlyDeferredRegister.create(StoneType.class, ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<StoneType>> STONE_TYPE_REGISTRY = REGISTER.makeRegistry("stone_type", () -> new RegistryBuilder<StoneType>()
            .setDefaultKey(new ResourceLocation(ProjectNublar.MODID, "granite")));

    public static RegistryObject<StoneType> GRANITE = REGISTER.register("granite", () -> new StoneType(1000, 0, Blocks.GRANITE::defaultBlockState, 50));
    public static RegistryObject<StoneType> DIORITE = REGISTER.register("diorite", () -> new StoneType(1000, 0, Blocks.DIORITE::defaultBlockState, 50));
    public static RegistryObject<StoneType> COBBLESTONE = REGISTER.register("cobblestone", () -> new StoneType(1000, 0, Blocks.COBBLESTONE::defaultBlockState, 50));
    public static RegistryObject<StoneType> CLAY = REGISTER.register("clay", () -> new StoneType(1000, 0, Blocks.CLAY::defaultBlockState, 50));
    public static RegistryObject<StoneType> TERRACOTTA = REGISTER.register("terracotta", () -> new StoneType(1000, 0, Blocks.TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> WHITE_TERRACOTTA = REGISTER.register("white_terracotta", () -> new StoneType(1000, 0, Blocks.WHITE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> ORANGE_TERRACOTTA = REGISTER.register("orange_terracotta", () -> new StoneType(1000, 0, Blocks.ORANGE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> MAGENTA_TERRACOTTA = REGISTER.register("magenta_terracotta", () -> new StoneType(1000, 0, Blocks.MAGENTA_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIGHT_BLUE_TERRACOTTA = REGISTER.register("light_blue_terracotta", () -> new StoneType(1000, 0, Blocks.LIGHT_BLUE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> YELLOW_TERRACOTTA = REGISTER.register("yellow_terracotta", () -> new StoneType(1000, 0, Blocks.YELLOW_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIME_TERRACOTTA = REGISTER.register("lime_terracotta", () -> new StoneType(1000, 0, Blocks.LIME_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> PINK_TERRACOTTA = REGISTER.register("pink_terracotta", () -> new StoneType(1000, 0, Blocks.PINK_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> GRAY_TERRACOTTA = REGISTER.register("gray_terracotta", () -> new StoneType(1000, 0, Blocks.GRAY_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> LIGHT_GRAY_TERRACOTTA = REGISTER.register("light_gray_terracotta", () -> new StoneType(1000, 0, Blocks.LIGHT_GRAY_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> CYAN_TERRACOTTA = REGISTER.register("cyan_terracotta", () -> new StoneType(1000, 0, Blocks.CYAN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> PURPLE_TERRACOTTA = REGISTER.register("purple_terracotta", () -> new StoneType(1000, 0, Blocks.PURPLE_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> BROWN_TERRACOTTA = REGISTER.register("brown_terracotta", () -> new StoneType(1000, 0, Blocks.BROWN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> GREEN_TERRACOTTA = REGISTER.register("green_terracotta", () -> new StoneType(1000, 0, Blocks.GREEN_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> RED_TERRACOTTA = REGISTER.register("red_terracotta", () -> new StoneType(1000, 0, Blocks.RED_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> BLACK_TERRACOTTA = REGISTER.register("black_terracotta", () -> new StoneType(1000, 0, Blocks.BLACK_TERRACOTTA::defaultBlockState, 50));
    public static RegistryObject<StoneType> NETHERRACK = REGISTER.register("netherrack", () -> new StoneType(1000, 0, Blocks.NETHERRACK::defaultBlockState, 50));
    public static RegistryObject<StoneType> BLACKSTONE = REGISTER.register("blackstone", () -> new StoneType(1000, 0, Blocks.BLACKSTONE::defaultBlockState, 50));
}
