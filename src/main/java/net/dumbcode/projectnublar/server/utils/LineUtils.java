package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.Mth;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LineUtils {
    //Returns {x, x1, z, z1, y, y1} Maybe swap around ?
    public static double[] intersect(BlockPos position, BlockPos fromPos, BlockPos toPos, double yoff) {
        Vector3d from = new Vector3d(fromPos.getX(), fromPos.getY(), fromPos.getZ()).add(0.5, yoff, 0.5);
        Vector3d to = new Vector3d(toPos.getX(), toPos.getY(), toPos.getZ()).add(0.5, yoff, 0.5);
        Optional<Vector3d> result = new AxisAlignedBB(position).clip(from, to);
        Optional<Vector3d> reverseResult = new AxisAlignedBB(position).clip(to, from);
        if(result.isPresent() && reverseResult.isPresent()) {
            Vector3d normal = result.get();
            Vector3d reverse = reverseResult.get();
            if(position.equals(new BlockPos(from))) {
                normal = from;
            } else if(position.equals(new BlockPos(to))) {
                reverse = to;
            }
            return new double[]{normal.x, reverse.x, normal.z, reverse.z, normal.y, reverse.y};
        }
        if(position.equals(fromPos) && !result.isPresent() && reverseResult.isPresent()) {
            Vector3d vec = reverseResult.get();
            return new double[] { from.x, vec.x, from.z, vec.z, from.y, vec.y };
        }
        if(position.equals(toPos) && !reverseResult.isPresent() && result.isPresent()) {
            Vector3d vec = result.get();
            return new double[] { vec.x, to.x, vec.z, to.z, vec.y, to.y };
        }
        return null;
    }
    public static List<BlockPos> getBlocksInbetween(BlockPos fromPos, BlockPos toPos, double offset) {
        Set<BlockPos> set = Sets.newLinkedHashSet();
        Vector3d from = new Vector3d(fromPos.getX() + 0.5, fromPos.getY() + offset, fromPos.getZ() + 0.5);
        Vector3d to = new Vector3d(toPos.getX() + 0.5, toPos.getY() + offset, toPos.getZ() + 0.5);


        if (!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z)) {
            if (!Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z)) {
                int i = Mth.floor(to.x);
                int j = Mth.floor(to.y);
                int k = Mth.floor(to.z);
                int l = Mth.floor(from.x);
                int i1 = Mth.floor(from.y);
                int j1 = Mth.floor(from.z);
                set.add(new BlockPos(l, i1, j1));
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(from.x) || Double.isNaN(from.y) || Double.isNaN(from.z)) {
                        set.add(new BlockPos(i, j, k));
                        break;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        set.add(new BlockPos(i, j, k));
                        break;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = to.x - from.x;
                    double d7 = to.y - from.y;
                    double d8 = to.z - from.z;

                    if (flag2) {
                        d3 = (d0 - from.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - from.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - from.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    Direction enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? Direction.WEST : Direction.EAST;
                        from = new Vector3d(d0, from.y + d7 * d3, from.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? Direction.DOWN : Direction.UP;
                        from = new Vector3d(from.x + d6 * d4, d1, from.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? Direction.NORTH : Direction.SOUTH;
                        from = new Vector3d(from.x + d6 * d5, from.y + d7 * d5, d2);
                    }

                    l = Mth.floor(from.x) - (enumfacing == Direction.EAST ? 1 : 0);
                    i1 = Mth.floor(from.y) - (enumfacing == Direction.UP ? 1 : 0);
                    j1 = Mth.floor(from.z) - (enumfacing == Direction.SOUTH ? 1 : 0);
                    BlockPos pos = new BlockPos(l, i1, j1);
                    double[] in = intersect(pos, fromPos, toPos, offset); //Surly a better way to do it
                    if(in != null && (in[0] != in[1] || in[2] != in[3] || in[4] != in[5])) {
                        set.add(pos);
                    }
                }
            }
        }

        return Lists.newArrayList(set);
    }

}
