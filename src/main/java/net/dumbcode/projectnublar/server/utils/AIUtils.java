package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@UtilityClass
public class AIUtils {

    /**
     * Gets all of the blocks in a circle with respect to the radius.
     *
     * @param h      center x position
     * @param y      entity y position
     * @param k      center z position
     * @param radius radius you want to search through
     * @return list of all block positions in the circle.
     */
    public static List<BlockPos> traverseXZ(int h, int y, int k, int radius)
    {
        List<BlockPos> blockPos = Lists.newArrayList();
        for (int x = h - radius; x <= h + radius; x++) {
            for (int z = k - radius; z <= k + radius; z++)
                if (findDistance(x, z, h, k) <= radius) {
                    blockPos.add(new BlockPos(x, y, z));
                }
        }
        return blockPos;
    }

    /**
     * Distance formula for a circle. Finds the distance to x and y
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param h x center coordinate
     * @param k y center coordinate
     * @return the distance to x and y from h and k, aka the radius
     */
    public static double findDistance(int x, int y, int h, int k) {
        return Math.sqrt(Math.pow((x - h), 2) + Math.pow((y - k), 2));
    }
}