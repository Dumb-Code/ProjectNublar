package net.dumbcode.projectnublar.server.block.entity;


import net.minecraft.item.DyeColor;

public interface DyableBlockEntity {
    void setDye(DyeColor color);
    DyeColor getDye();
}
