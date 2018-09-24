package net.dumbcode.projectnublar.server.utils;

import lombok.experimental.UtilityClass;

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
}
