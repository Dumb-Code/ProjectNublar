package net.dumbcode.projectnublar.server.fossil.impl;

import com.mojang.datafixers.util.Either;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.fossil.api.Extension;
import net.dumbcode.projectnublar.server.fossil.api.IFossilExtension;
import net.dumbcode.projectnublar.server.fossil.api.context.FossilRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.api.context.StoneTypeRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.OreFeatureConfig;

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
        context.registerStoneType(new StoneType(1000, 0, "granite", Blocks.GRANITE::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "andesite", Blocks.ANDESITE::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "diorite", Blocks.DIORITE::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "cobblestone", Blocks.COBBLESTONE::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "clay", Blocks.CLAY::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "terracotta", Blocks.TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "white_terracotta", Blocks.WHITE_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "orange_terracotta", Blocks.ORANGE_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "magenta_terracotta", Blocks.MAGENTA_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "light_blue_terracotta", Blocks.LIGHT_BLUE_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "yellow_terracotta", Blocks.YELLOW_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "lime_terracotta", Blocks.LIME_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "pink_terracotta", Blocks.PINK_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "terracotta", Blocks.GRAY_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "light_gray_terracotta", Blocks.LIGHT_GRAY_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "cyan_terracotta", Blocks.CYAN_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "purple_terracotta", Blocks.PURPLE_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "brown_terracotta", Blocks.BROWN_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "green_terracotta", Blocks.GREEN_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "red_terracotta", Blocks.RED_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "black_terracotta", Blocks.BLACK_TERRACOTTA::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "netherack", Blocks.NETHERRACK::defaultBlockState, 50));
        context.registerStoneType(new StoneType(1000, 0, "blackstone",Blocks.BLACKSTONE::defaultBlockState, 50));
   }

    @Override
    public String getName() {
        return "default";
    }
}
