package net.dumbcode.projectnublar.server.block.entity;

import net.minecraft.item.EnumDyeColor;

public interface DyableBlockEntity {
    void setDye(EnumDyeColor color);
    EnumDyeColor getDye();
}
