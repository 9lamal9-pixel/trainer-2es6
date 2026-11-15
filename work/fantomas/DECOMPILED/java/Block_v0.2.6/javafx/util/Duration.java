/*
 * Decompiled with CFR 0.152.
 */
package javafx.util;

import java.io.Serializable;
import javafx.beans.NamedArg;

public class Duration
implements Comparable<Duration>,
Serializable {
    public static final Duration ZERO = new Duration(0.0);
    public static final Duration ONE = new Duration(1.0);
    public static final Duration INDEFINITE = new Duration(Double.POSITIVE_INFINITY);
    public static final Duration UNKNOWN = new Duration(Double.NaN);
    private final double millis;

    public static Duration valueOf(String string) {
        int n = -1;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-') continue;
            n = i;
            break;
        }
        if (n == -1) {
            throw new IllegalArgumentException("The time parameter must have a suffix of [ms|s|m|h]");
        }
        double d = Double.parseDouble(string.substring(0, n));
        String string2 = string.substring(n);
        if ("ms".equals(string2)) {
            return Duration.millis(d);
        }
        if ("s".equals(string2)) {
            return Duration.seconds(d);
        }
        if ("m".equals(string2)) {
            return Duration.minutes(d);
        }
        if ("h".equals(string2)) {
            return Duration.hours(d);
        }
        throw new IllegalArgumentException("The time parameter must have a suffix of [ms|s|m|h]");
    }

    public static Duration millis(double d) {
        if (d == 0.0) {
            return ZERO;
        }
        if (d == 1.0) {
            return ONE;
        }
        if (d == Double.POSITIVE_INFINITY) {
            return INDEFINITE;
        }
        if (Double.isNaN(d)) {
            return UNKNOWN;
        }
        return new Duration(d);
    }

    public static Duration seconds(double d) {
        if (d == 0.0) {
            return ZERO;
        }
        if (d == Double.POSITIVE_INFINITY) {
            return INDEFINITE;
        }
        if (Double.isNaN(d)) {
            return UNKNOWN;
        }
        return new Duration(d * 1000.0);
    }

    public static Duration minutes(double d) {
        if (d == 0.0) {
            return ZERO;
        }
        if (d == Double.POSITIVE_INFINITY) {
            return INDEFINITE;
        }
        if (Double.isNaN(d)) {
            return UNKNOWN;
        }
        return new Duration(d * 60000.0);
    }

    public static Duration hours(double d) {
        if (d == 0.0) {
            return ZERO;
        }
        if (d == Double.POSITIVE_INFINITY) {
            return INDEFINITE;
        }
        if (Double.isNaN(d)) {
            return UNKNOWN;
        }
        return new Duration(d * 3600000.0);
    }

    public Duration(@NamedArg(value="millis") double d) {
        this.millis = d;
    }

    public double toMillis() {
        return this.millis;
    }

    public double toSeconds() {
        return this.millis / 1000.0;
    }

    public double toMinutes() {
        return this.millis / 60000.0;
    }

    public double toHours() {
        return this.millis / 3600000.0;
    }

    public Duration add(Duration duration) {
        return Duration.millis(this.millis + duration.millis);
    }

    public Duration subtract(Duration duration) {
        return Duration.millis(this.millis - duration.millis);
    }

    @Deprecated
    public Duration multiply(Duration duration) {
        return Duration.millis(this.millis * duration.millis);
    }

    public Duration multiply(double d) {
        return Duration.millis(this.millis * d);
    }

    public Duration divide(double d) {
        return Duration.millis(this.millis / d);
    }

    @Deprecated
    public Duration divide(Duration duration) {
        return Duration.millis(this.millis / duration.millis);
    }

    public Duration negate() {
        return Duration.millis(-this.millis);
    }

    public boolean isIndefinite() {
        return this.millis == Double.POSITIVE_INFINITY;
    }

    public boolean isUnknown() {
        return Double.isNaN(this.millis);
    }

    public boolean lessThan(Duration duration) {
        return this.millis < duration.millis;
    }

    public boolean lessThanOrEqualTo(Duration duration) {
        return this.millis <= duration.millis;
    }

    public boolean greaterThan(Duration duration) {
        return this.millis > duration.millis;
    }

    public boolean greaterThanOrEqualTo(Duration duration) {
        return this.millis >= duration.millis;
    }

    public String toString() {
        return this.isIndefinite() ? "INDEFINITE" : (this.isUnknown() ? "UNKNOWN" : this.millis + " ms");
    }

    @Override
    public int compareTo(Duration duration) {
        return Double.compare(this.millis, duration.millis);
    }

    public boolean equals(Object object) {
        return object == this || object instanceof Duration && this.millis == ((Duration)object).millis;
    }

    public int hashCode() {
        long l = Double.doubleToLongBits(this.millis);
        return (int)(l ^ l >>> 32);
    }
}

