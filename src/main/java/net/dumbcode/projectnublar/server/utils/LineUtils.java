package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class LineUtils {
    //Returns {x, x1, y, y1} Maybe swap x1 and y around ?
    public static double[] liangBarskyIntersect(double xMin, double xMax, double yMin, double yMax, double x0, double x1, double y0, double y1) {

        double u1 = 0;
        double u2 = 1;

        double dx = x1 - x0;
        double dy = y1 - y0;

        double[] gradients= {-dx, dx, -dy, dy};
        double[] points = {x0 - xMin, xMax - x0, y0 - yMin, yMax - y0};

        for (int i = 0; i < 4; i++) {
            if (gradients[i] == 0) {
                if (points[i] < 0) {
                    return null;
                }
            } else {
                double u = points[i] / gradients[i];
                if (gradients[i] < 0) {
                    u1 = Math.max(u, u1);
                } else {
                    u2 = Math.min(u, u2);
                }
            }
        }

        return new double[] {(x0 + u1 * dx), (x0 + u2 * dx), (y0 + u1 * dy), (y0 + u2 * dy)};
    }


    //http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html
    public static List<Vec2i> raytraceZX(double x0, double x1, double y0, double y1) {

        List<Vec2i> vecs = Lists.newArrayList();

        double dx = Math.abs(x1 - x0);
        double dy = Math.abs(y1 - y0);

        int x = MathHelper.floor(x0);
        int y = MathHelper.floor(y0);

        int n = 1;
        int xIncline;
        int yIncline;
        double error;

        if (dx == 0) {
            xIncline = 0;
            error = Double.POSITIVE_INFINITY;
        } else if (x1 > x0) {
            xIncline = 1;
            n += MathHelper.floor(x1) - x;
            error = (Math.floor(x0) + 1 - x0) * dy;
        } else {
            xIncline = -1;
            n += x - MathHelper.floor(x1);
            error = (x0 - Math.floor(x0)) * dy;
        }

        if (dy == 0) {
            yIncline = 0;
            error -= Double.POSITIVE_INFINITY;
        } else if (y1 > y0) {
            yIncline = 1;
            n += MathHelper.floor(y1) - y;
            error -= (Math.floor(y0) + 1 - y0) * dx;
        } else {
            yIncline = -1;
            n += y - MathHelper.floor(y1);
            error -= (y0 - Math.floor(y0)) * dx;
        }

        while (n > 0) {
            vecs.add(new Vec2i(x, y));
            if (error > 0) {
                y += yIncline;
                error -= dx;
            } else {
                x += xIncline;
                error += dy;
            }
            n--;
        }

        return vecs;
    }


    public static Set<BlockPos> getBlocksInbetween(BlockPos fromPos, BlockPos toPos, @Nullable Predicate<BlockPos> predicate) {
        Set<BlockPos> set = Sets.newHashSet();
        Vector3d from = new Vector3d(fromPos.getX()+0.5, fromPos.getY()+0.5, fromPos.getZ()+0.5);
        Vector3d to = new Vector3d(toPos.getX()+0.5, toPos.getY()+0.5, toPos.getZ()+0.5);
        double yrange = (from.getY() - to.getY()) / Math.sqrt((to.x-from.x)*(to.x-from.x) + (to.z-from.z)*(to.z-from.z));
        for (Vec2i vec : raytraceZX(to.getX(), from.getX(), to.getZ(), from.getZ())) {
            double squarepos = Math.sqrt((vec.x - to.getX())*(vec.x - to.getX()) + (vec.z - to.getZ())*(vec.z - to.getZ()));
            BlockPos position = new BlockPos(vec.x, to.getY() + yrange * squarepos, vec.z);
            if(predicate == null || predicate.test(position)) {
                set.add(position);
            }
        }
        return set;
    }

}
