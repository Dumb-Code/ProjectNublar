package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Maps;
import lombok.Value;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.Random;

@UtilityClass
public class MathUtils {

    public static int[] generateWeightedList(int size) {
        int[] out = new int[(size * (size - 1)) + 1]; //n(n-1)
        for (int i = 0; i < size; i++) {
            int base = (i * (i - 1));
            for (int i1 = 0; i1 <= 2*i; i1++) {
                out[base + i1] = size - i;
            }
        }
        return out;
    }

    public static int getWeightedResult(int size) {
        int[] aint = generateWeightedList(size);
        //Fisher–Yates shuffle
        Random rnd = new Random();
        for (int i = aint.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            int a = aint[index];
            aint[index] = aint[i];
            aint[i] = a;
        }
        return aint[new Random().nextInt(aint.length)];
    }

    public static int floorToZero(double value) {
        return value > 0 ? MathHelper.floor(value) : MathHelper.ceil(value);
    }

    public static int[] binChoose(int n) {
        n += 1;
        int[][] cache = new int[n+1][n];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j < Math.min(i, n); j++) {
                if(j == 0 || j == i) {
                    cache[i][j] = 1;
                } else {
                    cache[i][j] = cache[i-1][j-1] + cache[i-1][j];
                }
            }
        }

        return cache[n];
    }

    public static double binomialExp(double a, double b, int pow) {
        return binomialExp(a, b, binChoose(pow));
    }

    public static double binomialExp(double a, double b, int[] n) {
        double total = 0;
        for (int i = 0; i < n.length; i++) {
            total += n[i] * Math.pow(a, i) * Math.pow(b, n.length - i - 1);
        }
        return total;
    }

    public static double mean(double... data) {
        double total = 0;
        for (double datum : data) {
            total += datum;
        }
        return total / data.length;
    }

    public static double meanDeviation(double... data) {
        double mean = mean(data);

        double divationTotal = 0;
        for (double datum : data) {
            divationTotal += datum - mean;
        }
        return divationTotal / data.length;
    }

    private static Map<TriVec, Vec3d> NORMAL_CACHE = Maps.newHashMap();

    public static Vec3d calculateNormal(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        Vec3d pos1 = new Vec3d(x1, y1, z1);
        Vec3d pos2 = new Vec3d(x2, y2, z2);
        Vec3d pos3 = new Vec3d(x3, y3, z3);
        return NORMAL_CACHE.computeIfAbsent(new TriVec(pos1, pos2, pos3), v -> pos2.subtract(pos1).crossProduct(pos3.subtract(pos1)).normalize());
    }

    public static Vector3f calcualeNormalF(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        Vec3d vec = calculateNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        return new Vector3f((float)vec.x, (float)vec.y, (float)vec.z);
    }

    public static double horizontalDegree(double x, double z, boolean forward) {
        double angle = Math.atan(z / x);
        if(x < 0 == forward) {
            angle += Math.PI;
        }
        return angle * 180 / Math.PI;
    }

    @Value private static class TriVec { Vec3d pos1; Vec3d pos2; Vec3d pos3; }
}
