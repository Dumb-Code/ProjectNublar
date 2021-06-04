package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.function.Function;

public enum PoleFacing {
    NONE(null, aabb -> 0D),
    DOWN(Direction.DOWN, a -> a.maxY),
    NORTH(Direction.NORTH, a -> a.maxZ),
    WEST(Direction.WEST, a -> a.maxX),
    SOUTH(Direction.SOUTH, a -> a.minZ),
    EAST(Direction.EAST, a -> a.minX),
    UP(Direction.UP, a -> a.minY);

    private final Direction facing;
    private final Function<AxisAlignedBB, Double> getterFunc;

    PoleFacing(Direction facing, Function<AxisAlignedBB, Double> getterFunc) {
        this.facing = facing;
        this.getterFunc = getterFunc;
    }

    public Direction getFacing() {
        return facing;
    }

    public float apply(AxisAlignedBB aabb) {
        return this.getterFunc.apply(aabb).floatValue();
    }

    public static PoleFacing getFromFacing(Direction facing) {
        for (PoleFacing poleFacing : PoleFacing.values()) {
            if(poleFacing.facing == facing) {
                return poleFacing;
            }
        }
        return NONE;
    }

    public PoleFacing cycle() {
        return PoleFacing.values()[(this.ordinal() + 1) % PoleFacing.values().length];
    }
}
