package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

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
}
