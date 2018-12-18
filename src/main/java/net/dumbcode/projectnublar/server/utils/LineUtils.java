package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class LineUtils {
    //Returns {x, x1, z, z1, y, y1} Maybe swap around ?
    public static double[] intersect(BlockPos position, BlockPos fromPos, BlockPos toPos, double yoff) {
        Vec3d from = new Vec3d(fromPos).addVector(0.5, yoff, 0.5);
        Vec3d to = new Vec3d(toPos).addVector(0.5, yoff, 0.5);
        RayTraceResult result = new AxisAlignedBB(position).calculateIntercept(from, to);
        RayTraceResult reverseResult = new AxisAlignedBB(position).calculateIntercept(to, from);
        if(result != null && reverseResult != null) {
            Vec3d normal = result.hitVec;
            Vec3d reverse = reverseResult.hitVec;
            if(position.equals(new BlockPos(from))) {
                normal = from;
            } else if(position.equals(new BlockPos(to))) {
                reverse = to;
            }
            return new double[]{normal.x, reverse.x, normal.z, reverse.z, normal.y, reverse.y};
        }
        return null;
    }
    public static List<BlockPos> getBlocksInbetween(BlockPos fromPos, BlockPos toPos, @Nullable Predicate<BlockPos> predicate) {
        List<BlockPos> set = Lists.newArrayList();
        for (int t = 0; t < 2; t++) {
            Vec3d from = new Vec3d(fromPos.getX() + 0.5, fromPos.getY() + 0.25 + 0.5*t, fromPos.getZ() + 0.5);
            Vec3d to = new Vec3d(toPos.getX() + 0.5, toPos.getY() + 0.25 + 0.5*t, toPos.getZ() + 0.5);

            if (!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z)) {
                if (!Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z)) {
                    int i = MathHelper.floor(to.x);
                    int j = MathHelper.floor(to.y);
                    int k = MathHelper.floor(to.z);
                    int l = MathHelper.floor(from.x);
                    int i1 = MathHelper.floor(from.y);
                    int j1 = MathHelper.floor(from.z);
                    BlockPos blockpos = new BlockPos(l, i1, j1);
                    if (predicate == null || predicate.test(blockpos)) {
                        set.add(blockpos);
                    }
                    blockpos = new BlockPos(i, j, k);
                    if (predicate == null || predicate.test(blockpos)) {
                        set.add(blockpos);
                    }

                    int k1 = 200;

                    while (k1-- >= 0) {
                        if (Double.isNaN(from.x) || Double.isNaN(from.y) || Double.isNaN(from.z)) {
                            break;
                        }

                        if (l == i && i1 == j && j1 == k) {
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

                        EnumFacing enumfacing;

                        if (d3 < d4 && d3 < d5) {
                            enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                            from = new Vec3d(d0, from.y + d7 * d3, from.z + d8 * d3);
                        } else if (d4 < d5) {
                            enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                            from = new Vec3d(from.x + d6 * d4, d1, from.z + d8 * d4);
                        } else {
                            enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                            from = new Vec3d(from.x + d6 * d5, from.y + d7 * d5, d2);
                        }

                        l = MathHelper.floor(from.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                        i1 = MathHelper.floor(from.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                        j1 = MathHelper.floor(from.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                        blockpos = new BlockPos(l, i1, j1);
                        if (predicate == null || predicate.test(blockpos)) {
                            set.add(blockpos);
                        }
                    }
                }
            }
        }
        
        return set;
    }

}
