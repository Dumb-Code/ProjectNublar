package net.dumbcode.projectnublar.server.fossil.impl;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.api.Extension;
import net.dumbcode.projectnublar.server.fossil.api.IFossilExtension;
import net.dumbcode.projectnublar.server.fossil.api.context.FossilRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.api.context.StoneTypeRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

@Extension
public class DefaultFossilExtension implements IFossilExtension {
    @Override
    public void registerFossils(FossilRegistrationContext context) {
        context.registerFossil(new Fossil(201, 66, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/ammonite"), "Ammonite", true));
        context.registerFossil(new Fossil(150, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/feather"), "Feather", true));
        context.registerFossil(new Fossil(365, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/feet"), "Feet", true));
        context.registerFossil(new Fossil(530, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/fish"), "Fish", true));
        context.registerFossil(new Fossil(275, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/imprint"), "Imprint", true));
        context.registerFossil(new Fossil(260, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/lizard"), "Lizard", true));
        context.registerFossil(new Fossil(500, 0, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/tooth"), "Tooth", true));
        context.registerFossil(new Fossil(521, 320, null, new ResourceLocation(ProjectNublar.MODID, "block/fossil/trilobite"), "Trilobite", true));
    }

    @Override
    public void registerStoneTypes(StoneTypeRegistrationContext context) {
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/stone"), "Stone", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/granite"), "Granite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/andesite"), "Andesite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/diorite"), "Diorite", "mineable/pickaxe", Material.STONE, 0, 1.5F, 6.0F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/clay"), "Stone", "mineable/pickaxe", Material.STONE, 0, 0.6F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/terracotta"), "Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/white_terracotta"), "White Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/orange_terracotta"), "Orange Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/magenta_terracotta"), "Magenta Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/light_blue_terracotta"), "Light Blue Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/yellow_terracotta"), "Yellow Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/lime_terracotta"), "Lime Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/pink_terracotta"), "Pink Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/gray_terracotta"), "Gray Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/light_gray_terracotta"), "Light Gray Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/cyan_terracotta"), "Cyan Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/purple_terracotta"), "Purple Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/blue_terracotta"), "Blue Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/brown_terracotta"), "Brown Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,158, 120, 104), new ResourceLocation("block/green_terracotta"), "Green Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,145, 145, 145), new ResourceLocation("block/red_terracotta"), "Red Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
        context.registerStoneType(new StoneType(1000, 0, context.color(255,179, 179, 179), new ResourceLocation("block/black_terracotta"), "Black Terracotta", "mineable/pickaxe", Material.STONE, 0, 1.25F, 4.2F));
    }

    @Override
    public String getName() {
        return "default";
    }
}
