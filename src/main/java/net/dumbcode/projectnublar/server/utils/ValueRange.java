package net.dumbcode.projectnublar.server.utils;

import javax.annotation.Nullable;

public class ValueRange {
    @Nullable
    private final Double min;
    @Nullable
    private final Double max;

    private ValueRange(@Nullable Double min, @Nullable Double max) {
        if(min != null && max != null && min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    public boolean inRange(double testValue) {
        boolean result = true;

        if(this.min != null) {
            result &= testValue >= this.min;
        }

        if(this.max != null) {
            result &= testValue <= this.max;
        }

        return result;
    }

    public static ValueRange inifinty() {
        return new ValueRange(null, null);
    }

    public static ValueRange upperBound(double bound) {
        return new ValueRange(null, bound);
    }

    public static ValueRange lowerBound(double bound) {
        return new ValueRange(bound, null);
    }

    public static ValueRange range(double lower, double upper) {
        return new ValueRange(lower, upper);
    }

}
