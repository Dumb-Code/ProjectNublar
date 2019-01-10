package net.dumbcode.projectnublar.server.entity;

import net.minecraft.util.IStringSerializable;

public enum ModelStage implements IStringSerializable {
    INFANT, CHILD, ADOLESCENCE, ADULT, DEAD;

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }
}
