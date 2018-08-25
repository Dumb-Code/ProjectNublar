package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum PoleFacing {
    NONE(null, aabb -> 0D),
    DOWN(EnumFacing.DOWN, a -> a.maxY),
    UP(EnumFacing.UP, a -> a.minY),
    NORTH(EnumFacing.NORTH, a -> a.maxZ),
    SOUTH(EnumFacing.SOUTH, a -> a.minZ),
    WEST(EnumFacing.WEST, a -> a.maxX),
    EAST(EnumFacing.EAST, a -> a.minX);

    private final EnumFacing facing;
    private final Function<AxisAlignedBB, Double> getterFunc;

    PoleFacing(EnumFacing facing, Function<AxisAlignedBB, Double> getterFunc) {
        this.facing = facing;
        this.getterFunc = getterFunc;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public double apply(AxisAlignedBB aabb) {
        return this.getterFunc.apply(aabb);
    }

    public static PoleFacing getFromFacing(EnumFacing facing) {
        for (PoleFacing poleFacing : PoleFacing.values()) {
            if(poleFacing.facing == facing) {
                return poleFacing;
            }
        }
        return NONE;
    }
}
