package net.dumbcode.projectnublar.server.utils;

import net.minecraft.util.Direction;

public class DirectionUtils {

    //Code below is copied from 1.12
    /**
     * Rotate this Facing around the given axis clockwise. If this facing cannot be rotated around the given axis,
     * returns this facing without rotating.
     */
    public static Direction rotateAround(Direction thiz, Direction.Axis axis)
    {
        switch (axis)
        {
            case X:

                if (thiz != Direction.WEST && thiz != Direction.EAST)
                {
                    return rotateX(thiz);
                }

                return thiz;
            case Y:

                if (thiz != Direction.UP && thiz != Direction.DOWN)
                {
                    return rotateY(thiz);
                }

                return thiz;
            case Z:

                if (thiz != Direction.NORTH && thiz != Direction.SOUTH)
                {
                    return rotateZ(thiz);
                }

                return thiz;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    /**
     * Rotate this Facing around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
     */
    public static Direction rotateY(Direction thiz)
    {
        switch (thiz)
        {
            case NORTH:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case WEST:
                return Direction.NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + thiz);
        }
    }

    /**
     * Rotate this Facing around the X axis (NORTH => DOWN => SOUTH => UP => NORTH)
     */
    public static Direction rotateX(Direction thiz)
    {
        switch (thiz)
        {
            case NORTH:
                return Direction.DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + thiz);
            case SOUTH:
                return Direction.UP;
            case UP:
                return Direction.NORTH;
            case DOWN:
                return Direction.SOUTH;
        }
    }

    /**
     * Rotate this Facing around the Z axis (EAST => DOWN => WEST => UP => EAST)
     */
    public static Direction rotateZ(Direction thiz)
    {
        switch (thiz)
        {
            case EAST:
                return Direction.DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + thiz);
            case WEST:
                return Direction.UP;
            case UP:
                return Direction.EAST;
            case DOWN:
                return Direction.WEST;
        }
    }
}
