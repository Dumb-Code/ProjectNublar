package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class MathUtils {

    public static int[] generateWeightedList(int size) {
        int[] out = new int[(size * (size + 1)) / 2]; //Triangular number sequence
        for (int i = 0; i < size; i++) {
            int base = (i * (i + 1)) / 2;
            for (int i1 = 0; i1 <= i; i1++) {
                out[base + i1] = 50 - i;
            }
        }
        return out;
    }
}
