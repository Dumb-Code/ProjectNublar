package net.dumbcode.projectnublar.server.world.structures;

import lombok.Getter;
import net.dumbcode.projectnublar.server.world.structures.structures.template.data.DataHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
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
        BlockPos mut = position.add(-xSize/2, 0, -zSize/2);
        while (mut.getY() > 0 && !world.isSideSolid(mut.down(), EnumFacing.UP)) {
            mut = mut.down();
        }
        this.position = mut;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    public boolean canBuild() {
        return true;
    }

    public abstract void build(Random random, List<DataHandler> handlers);
}
