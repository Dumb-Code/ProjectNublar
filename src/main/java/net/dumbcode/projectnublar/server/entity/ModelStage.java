package net.dumbcode.projectnublar.server.entity;

import net.minecraft.util.IStringSerializable;

//TODO: remove this fucker
public enum ModelStage implements IStringSerializable {
    INFANT(12000), //10 minutes
    CHILD(36000), //30 minutes
    ADOLESCENCE(30000), //25 minutes
    ADULT(-1),
    SKELETON(-1);

    private final int defaultTickAge;

    ModelStage(int defaultTickAge) {
        this.defaultTickAge = defaultTickAge;
    }

    public int getDefaultTickAge() {
        return defaultTickAge;
    }

    @Override
    public String getName() {
        return this.name().toLowerCase();
    }
}
