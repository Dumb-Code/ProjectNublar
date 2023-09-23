package net.dumbcode.projectnublar.server.fossil.impl;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.api.Extension;
import net.dumbcode.projectnublar.server.fossil.api.IFossilExtension;
import net.dumbcode.projectnublar.server.fossil.api.context.FossilRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.api.context.StoneTypeRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.SameTextureStoneType;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.Collections;

@Extension
public class DefaultFossilExtension implements IFossilExtension {
    @Override
    public void registerFossils(FossilRegistrationContext context) {
        //"foot", "claw", "leg", "neck", "pelvis", "ribcage", "skull", "tail"
        //TODO: set all the item textures on these
        context.registerFossil(new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/ammonite"), "Ammonite", true, null, null, null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feather"), "Dilophosaurus Feather", true, DinosaurHandler.DILOPHOSAURUS, "feather", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feather"), "Tyrannosaurus Feather", true, DinosaurHandler.TYRANNOSAURUS, "feather", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feather"), "Velociraptor JP Feather", true, DinosaurHandler.VELOCIRAPTOR_JP, "feather", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feather"), "Velociraptor JP 3 Feather", true, DinosaurHandler.VELOCIRAPTOR_JP3, "feather", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feet"), "Dilophosaurus Feet", true, DinosaurHandler.DILOPHOSAURUS, "foot", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feet"), "Tyrannosaurus Feet", true, DinosaurHandler.TYRANNOSAURUS, "foot", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feet"), "Velociraptor JP Feet", true, DinosaurHandler.VELOCIRAPTOR_JP, "foot", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/feet"), "Velociraptor JP 3 Feet", true, DinosaurHandler.VELOCIRAPTOR_JP3, "foot", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/tooth"), "Dilophosaurus Tooth", true, DinosaurHandler.DILOPHOSAURUS, "tooth", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/tooth"), "Tyrannosaurus Tooth", true, DinosaurHandler.TYRANNOSAURUS, "tooth", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/tooth"), "Velociraptor JP Tooth", true, DinosaurHandler.VELOCIRAPTOR_JP, "tooth", null));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/tooth"), "Velociraptor JP 3 Tooth", true, DinosaurHandler.VELOCIRAPTOR_JP3, "tooth", null));
        context.registerFossil(new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/trilobite"), "Trilobite", true, null, null, null));
    }

    @Override
    public void registerStoneTypes(StoneTypeRegistrationContext context) {
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/granite"), "Granite", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.GRANITE), 50, 0, 1.5F, 6.0F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/andesite"), "Andesite", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.ANDESITE), 50, 0, 1.5F, 6.0F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/diorite"), "Diorite", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.DIORITE), 50, 0, 1.5F, 6.0F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/cobblestone"), "Cobblestone", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.COBBLESTONE), 50, 0, 1.5F, 6.0F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/clay"), "Clay", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.CLAY), 50, 0, 0.6F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/terracotta"), "Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/white_terracotta"), "White Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.WHITE_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/orange_terracotta"), "Orange Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.ORANGE_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/magenta_terracotta"), "Magenta Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.MAGENTA_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/light_blue_terracotta"), "Light Blue Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.LIGHT_BLUE_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/yellow_terracotta"), "Yellow Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.YELLOW_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/lime_terracotta"), "Lime Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.LIME_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/pink_terracotta"), "Pink Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.PINK_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/gray_terracotta"), "Gray Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.GRAY_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/light_gray_terracotta"), "Light Gray Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.LIGHT_GRAY_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/cyan_terracotta"), "Cyan Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.CYAN_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/purple_terracotta"), "Purple Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.PURPLE_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/blue_terracotta"), "Blue Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.BLUE_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/brown_terracotta"), "Brown Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.BROWN_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/green_terracotta"), "Green Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.GREEN_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/red_terracotta"), "Red Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.RED_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/black_terracotta"), "Black Terracotta", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NATURAL_STONE, Either.right(Blocks.BLACK_TERRACOTTA), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/netherrack"), "Netherrack", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NETHER_ORE_REPLACEABLES, Either.right(Blocks.NETHERRACK), 50, 0, 1.25F, 4.2F));
        context.registerStoneType(new SameTextureStoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/blackstone"), "Blackstone", "mineable/pickaxe", Material.STONE, OreFeatureConfig.FillerBlockType.NETHER_ORE_REPLACEABLES, Either.right(Blocks.BLACKSTONE), 50, 0, 1.25F, 4.2F));
    }

    @Override
    public String getName() {
        return "default";
    }
}
