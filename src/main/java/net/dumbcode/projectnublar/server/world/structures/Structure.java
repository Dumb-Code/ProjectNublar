package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;

@Getter
public class Structure {
    private final String name;
    private final int xSize;
    private final int zSize;
    private final int weight;
    private final int children;

    public Structure(String name, int xSize, int zSize, int weight, int children) {
        this.name = name;
        this.xSize = xSize;
        this.zSize = zSize;
        this.weight = weight;
        this.children = children;
    }

    @Override
    public String toString() {
        return name;
    }
}
