/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.scenario.animation.NumberTangentInterpolator;
import com.sun.scenario.animation.SplineInterpolator;
import javafx.animation.Interpolatable;
import javafx.util.Duration;

public abstract class Interpolator {
    private static final double EPSILON = 1.0E-12;
    public static final Interpolator DISCRETE = new Interpolator(){

        @Override
        protected double curve(double d) {
            return Math.abs(d - 1.0) < 1.0E-12 ? 1.0 : 0.0;
        }

        public String toString() {
            return "Interpolator.DISCRETE";
        }
    };
    public static final Interpolator LINEAR = new Interpolator(){

        @Override
        protected double curve(double d) {
            return d;
        }

        public String toString() {
            return "Interpolator.LINEAR";
        }
    };
    public static final Interpolator EASE_BOTH = new Interpolator(){

        @Override
        protected double curve(double d) {
            return Interpolator.clamp(d < 0.2 ? 3.125 * d * d : (d > 0.8 ? -3.125 * d * d + 6.25 * d - 2.125 : 1.25 * d - 0.125));
        }

        public String toString() {
            return "Interpolator.EASE_BOTH";
        }
    };
    public static final Interpolator EASE_IN = new Interpolator(){
        private static final double S1 = 2.7777777777777777;
        private static final double S3 = 1.1111111111111112;
        private static final double S4 = 0.1111111111111111;

        @Override
        protected double curve(double d) {
            return Interpolator.clamp(d < 0.2 ? 2.7777777777777777 * d * d : 1.1111111111111112 * d - 0.1111111111111111);
        }

        public String toString() {
            return "Interpolator.EASE_IN";
        }
    };
    public static final Interpolator EASE_OUT = new Interpolator(){
        private static final double S1 = -2.7777777777777777;
        private static final double S2 = 5.555555555555555;
        private static final double S3 = -1.7777777777777777;
        private static final double S4 = 1.1111111111111112;

        @Override
        protected double curve(double d) {
            return Interpolator.clamp(d > 0.8 ? -2.7777777777777777 * d * d + 5.555555555555555 * d + -1.7777777777777777 : 1.1111111111111112 * d);
        }

        public String toString() {
            return "Interpolator.EASE_OUT";
        }
    };

    protected Interpolator() {
    }

    public static Interpolator SPLINE(double d, double d2, double d3, double d4) {
        return new SplineInterpolator(d, d2, d3, d4);
    }

    public static Interpolator TANGENT(Duration duration, double d, Duration duration2, double d2) {
        return new NumberTangentInterpolator(duration, d, duration2, d2);
    }

    public static Interpolator TANGENT(Duration duration, double d) {
        return new NumberTangentInterpolator(duration, d);
    }

    public Object interpolate(Object object, Object object2, double d) {
        if (object instanceof Number && object2 instanceof Number) {
            double d2 = ((Number)object).doubleValue();
            double d3 = ((Number)object2).doubleValue();
            double d4 = d2 + (d3 - d2) * this.curve(d);
            if (object instanceof Double || object2 instanceof Double) {
                return d4;
            }
            if (object instanceof Float || object2 instanceof Float) {
                return Float.valueOf((float)d4);
            }
            if (object instanceof Long || object2 instanceof Long) {
                return Math.round(d4);
            }
            return (int)Math.round(d4);
        }
        if (object instanceof Interpolatable && object2 instanceof Interpolatable) {
            return ((Interpolatable)object).interpolate(object2, this.curve(d));
        }
        return this.curve(d) == 1.0 ? object2 : object;
    }

    public boolean interpolate(boolean bl, boolean bl2, double d) {
        return Math.abs(this.curve(d) - 1.0) < 1.0E-12 ? bl2 : bl;
    }

    public double interpolate(double d, double d2, double d3) {
        return d + (d2 - d) * this.curve(d3);
    }

    public int interpolate(int n, int n2, double d) {
        return n + (int)Math.round((double)(n2 - n) * this.curve(d));
    }

    public long interpolate(long l, long l2, double d) {
        return l + Math.round((double)(l2 - l) * this.curve(d));
    }

    private static double clamp(double d) {
        return d < 0.0 ? 0.0 : (d > 1.0 ? 1.0 : d);
    }

    protected abstract double curve(double var1);
}

