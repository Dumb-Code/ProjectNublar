package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;
import java.util.List;
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

    public static Vec3d calculateNormal(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        Vec3d pos1 = new Vec3d(x1, y1, z1);
        Vec3d pos2 = new Vec3d(x2, y2, z2);
        Vec3d pos3 = new Vec3d(x3, y3, z3);
        return pos2.subtract(pos1).crossProduct(pos3.subtract(pos1)).normalize();
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
}
