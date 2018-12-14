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


    public static List<Vec2i> bresenhamYCoords(double x0, double x1, double y0, double y1) {
        if(x1 < x0) {
            double temp = x0;
            x0 = x1;
            x1 = temp;

            temp = y0;
            y0 = y1;
            y1 = temp;
        }

        double xoffStart = Math.ceil(x0) - x0;
        double yoffStart = Math.ceil(y0) - y0;

        double dx = x1 - x0;
        double dy = y1 - y0;

        if(Math.abs(dx) < 1) { //Don't divide by less than 1
            List<Vec2i> out = Lists.newArrayList();
            for (int y = MathHelper.floor(y0); y != MathHelper.ceil(y1); y += Math.signum(dy)) {
                out.add(new Vec2i(MathHelper.floor(x0), y));
            }
            return out;
        }

        double deltaerror = dy / dx;
        double y = Math.floor(y0);

        double error = xoffStart * deltaerror + yoffStart;

        List<Vec2i> out = Lists.newArrayList();
        for (int x = MathHelper.floor(x0); x < x1; x ++) {
            while(Math.abs(error) > 1) {
                out.add(new Vec2i(x, (int)y));
                y += Math.signum(dy);
                error -= Math.signum(error);
            }
            error += deltaerror;
            out.add(new Vec2i(x, (int)(y)));
        }
        return out;
    }


    public static Set<BlockPos> getBlocksInbetween(BlockPos fromPos, BlockPos toPos, @Nullable Predicate<BlockPos> predicate) {
        Set<BlockPos> set = Sets.newHashSet();
        Vector3d from = new Vector3d(fromPos.getX()+0.5, fromPos.getY()+0.5, fromPos.getZ()+0.5);
        Vector3d to = new Vector3d(toPos.getX()+0.5, toPos.getY()+0.5, toPos.getZ()+0.5);
        double yrange = (from.getY() - to.getY()) / Math.sqrt((to.x-from.x)*(to.x-from.x) + (to.z-from.z)*(to.z-from.z));
        for (Vec2i vec : bresenhamYCoords(to.getX(), from.getX(), to.getZ(), from.getZ())) {
            double squarepos = Math.sqrt((vec.x - to.getX())*(vec.x - to.getX()) + (vec.z - to.getZ())*(vec.z - to.getZ()));
            BlockPos position = new BlockPos(vec.x, to.getY() + yrange * squarepos, vec.z);
            if(predicate == null || predicate.test(position)) {
                set.add(position);
            }
        }
        return set;
    }

}
