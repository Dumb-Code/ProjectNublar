package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.math.MathHelper;

import java.util.List;

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
        double dx = x1 - x0;
        double dy = y1 - y0;

        if(Math.abs(dx) < 1) { //Don't divide by less than 1
            List<Vec2i> out = Lists.newArrayList();
            for (int y = MathHelper.floor(y0); y != MathHelper.ceil(y1); y += Math.signum(dy)) {
                out.add(new Vec2i(MathHelper.floor(x0), y));
            }
            return out;
        }

        double deltaerror = Math.abs(dy / dx);
        double error = 0;

        int y = MathHelper.floor(y0);

        List<Vec2i> out = Lists.newArrayList();
        for (int x = MathHelper.floor(x0); x != MathHelper.ceil(x1); x += Math.signum(dx)) {
            error += deltaerror;
            while(error > 1) {
                out.add(new Vec2i(x, y));
                y += Math.signum(dy);
                error--;
            }
            out.add(new Vec2i(x, y));
        }
        return out;
    }

}
