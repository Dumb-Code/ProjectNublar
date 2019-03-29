package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@Getter
public abstract class StructureInstance {

    protected final World world;
    protected final BlockPos position;

    protected final int children;

    protected final int xSize;
    protected final int zSize;

    public StructureInstance(World world, BlockPos position, int children, int xSize, int zSize) {
        this.world = world;
        this.children = children;
        this.position = position.add(-xSize/2, 0, -zSize/2);
        this.xSize = xSize;
        this.zSize = zSize;
    }

    public boolean canBuild() {
        return true;
    }

    public abstract void build(Random random);
}
