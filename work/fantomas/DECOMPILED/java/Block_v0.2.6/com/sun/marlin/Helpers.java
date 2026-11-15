/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;
import com.sun.marlin.ByteArrayCache;
import com.sun.marlin.Curve;
import com.sun.marlin.FloatArrayCache;
import com.sun.marlin.IntArrayCache;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.RendererContext;
import com.sun.marlin.stats.Histogram;
import com.sun.marlin.stats.StatLong;
import java.util.Arrays;

final class Helpers
implements MarlinConst {
    private Helpers() {
        throw new Error("This is a non instantiable class");
    }

    static boolean within(float f, float f2, float f3) {
        float f4 = f2 - f;
        return f4 <= f3 && f4 >= -f3;
    }

    static boolean within(double d, double d2, double d3) {
        double d4 = d2 - d;
        return d4 <= d3 && d4 >= -d3;
    }

    static float evalCubic(float f, float f2, float f3, float f4, float f5) {
        return f5 * (f5 * (f5 * f + f2) + f3) + f4;
    }

    static float evalQuad(float f, float f2, float f3, float f4) {
        return f4 * (f4 * f + f2) + f3;
    }

    static int quadraticRoots(float f, float f2, float f3, float[] fArray, int n) {
        int n2 = n;
        if (f != 0.0f) {
            float f4 = f2 * f2 - 4.0f * f * f3;
            if (f4 > 0.0f) {
                float f5 = (float)Math.sqrt(f4);
                if (f2 >= 0.0f) {
                    fArray[n2++] = 2.0f * f3 / (-f2 - f5);
                    fArray[n2++] = (-f2 - f5) / (2.0f * f);
                } else {
                    fArray[n2++] = (-f2 + f5) / (2.0f * f);
                    fArray[n2++] = 2.0f * f3 / (-f2 + f5);
                }
            } else if (f4 == 0.0f) {
                fArray[n2++] = -f2 / (2.0f * f);
            }
        } else if (f2 != 0.0f) {
            fArray[n2++] = -f3 / f2;
        }
        return n2 - n;
    }

    static int cubicRootsInAB(float f, float f2, float f3, float f4, float[] fArray, int n, float f5, float f6) {
        int n2;
        if (f == 0.0f) {
            int n3 = Helpers.quadraticRoots(f2, f3, f4, fArray, n);
            return Helpers.filterOutNotInAB(fArray, n, n3, f5, f6) - n;
        }
        double d = (double)f2 / (double)f;
        double d2 = d * d;
        double d3 = 0.3333333333333333 * d;
        double d4 = (double)f3 / (double)f;
        double d5 = (double)f4 / (double)f;
        double d6 = 0.5 * (0.07407407407407407 * d * d2 - d3 * d4 + d5);
        double d7 = 0.3333333333333333 * (-0.3333333333333333 * d2 + d4);
        double d8 = d7 * d7 * d7;
        double d9 = d6 * d6 + d8;
        if (d9 < 0.0) {
            double d10 = 0.3333333333333333 * Math.acos(-d6 / Math.sqrt(-d8));
            double d11 = 2.0 * Math.sqrt(-d7);
            fArray[n] = (float)(d11 * Math.cos(d10) - d3);
            fArray[n + 1] = (float)(-d11 * Math.cos(d10 + 1.0471975511965976) - d3);
            fArray[n + 2] = (float)(-d11 * Math.cos(d10 - 1.0471975511965976) - d3);
            n2 = 3;
        } else {
            double d12 = Math.sqrt(d9);
            double d13 = Math.cbrt(d12 - d6);
            double d14 = -Math.cbrt(d12 + d6);
            fArray[n] = (float)(d13 + d14 - d3);
            n2 = 1;
            if (Helpers.within(d9, 0.0, 1.0E-8)) {
                fArray[n + 1] = (float)(-0.5 * (d13 + d14) - d3);
                n2 = 2;
            }
        }
        return Helpers.filterOutNotInAB(fArray, n, n2, f5, f6) - n;
    }

    static int filterOutNotInAB(float[] fArray, int n, int n2, float f, float f2) {
        int n3 = n;
        int n4 = n + n2;
        for (int i = n; i < n4; ++i) {
            if (!(fArray[i] >= f) || !(fArray[i] < f2)) continue;
            fArray[n3++] = fArray[i];
        }
        return n3;
    }

    static float fastLineLen(float f, float f2, float f3, float f4) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        return Math.abs(f5) + Math.abs(f6);
    }

    static float linelen(float f, float f2, float f3, float f4) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        return (float)Math.sqrt(f5 * f5 + f6 * f6);
    }

    static float fastQuadLen(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = f3 - f;
        float f8 = f5 - f3;
        float f9 = f4 - f2;
        float f10 = f6 - f4;
        return Math.abs(f7) + Math.abs(f8) + Math.abs(f9) + Math.abs(f10);
    }

    static float quadlen(float f, float f2, float f3, float f4, float f5, float f6) {
        return (Helpers.linelen(f, f2, f3, f4) + Helpers.linelen(f3, f4, f5, f6) + Helpers.linelen(f, f2, f5, f6)) / 2.0f;
    }

    static float fastCurvelen(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        float f9 = f3 - f;
        float f10 = f5 - f3;
        float f11 = f7 - f5;
        float f12 = f4 - f2;
        float f13 = f6 - f4;
        float f14 = f8 - f6;
        return Math.abs(f9) + Math.abs(f10) + Math.abs(f11) + Math.abs(f12) + Math.abs(f13) + Math.abs(f14);
    }

    static float curvelen(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        return (Helpers.linelen(f, f2, f3, f4) + Helpers.linelen(f3, f4, f5, f6) + Helpers.linelen(f5, f6, f7, f8) + Helpers.linelen(f, f2, f7, f8)) / 2.0f;
    }

    static int findSubdivPoints(Curve curve, float[] fArray, float[] fArray2, int n, float f) {
        float f2 = fArray[2] - fArray[0];
        float f3 = fArray[3] - fArray[1];
        if (f3 != 0.0f && f2 != 0.0f) {
            float f4 = (float)Math.sqrt(f2 * f2 + f3 * f3);
            float f5 = f2 / f4;
            float f6 = f3 / f4;
            float f7 = f5 * fArray[0] + f6 * fArray[1];
            float f8 = f5 * fArray[1] - f6 * fArray[0];
            float f9 = f5 * fArray[2] + f6 * fArray[3];
            float f10 = f5 * fArray[3] - f6 * fArray[2];
            float f11 = f5 * fArray[4] + f6 * fArray[5];
            float f12 = f5 * fArray[5] - f6 * fArray[4];
            switch (n) {
                case 8: {
                    float f13 = f5 * fArray[6] + f6 * fArray[7];
                    float f14 = f5 * fArray[7] - f6 * fArray[6];
                    curve.set(f7, f8, f9, f10, f11, f12, f13, f14);
                    break;
                }
                case 6: {
                    curve.set(f7, f8, f9, f10, f11, f12);
                    break;
                }
            }
        } else {
            curve.set(fArray, n);
        }
        int n2 = 0;
        n2 += curve.dxRoots(fArray2, n2);
        n2 += curve.dyRoots(fArray2, n2);
        if (n == 8) {
            n2 += curve.infPoints(fArray2, n2);
        }
        n2 += curve.rootsOfROCMinusW(fArray2, n2, f, 1.0E-4f);
        n2 = Helpers.filterOutNotInAB(fArray2, 0, n2, 1.0E-4f, 0.9999f);
        Helpers.isort(fArray2, n2);
        return n2;
    }

    static int findClipPoints(Curve curve, float[] fArray, float[] fArray2, int n, int n2, float[] fArray3) {
        curve.set(fArray, n);
        int n3 = 0;
        if ((n2 & 4) != 0) {
            n3 += curve.xPoints(fArray2, n3, fArray3[2]);
        }
        if ((n2 & 8) != 0) {
            n3 += curve.xPoints(fArray2, n3, fArray3[3]);
        }
        if ((n2 & 1) != 0) {
            n3 += curve.yPoints(fArray2, n3, fArray3[0]);
        }
        if ((n2 & 2) != 0) {
            n3 += curve.yPoints(fArray2, n3, fArray3[1]);
        }
        Helpers.isort(fArray2, n3);
        return n3;
    }

    static void subdivide(float[] fArray, float[] fArray2, float[] fArray3, int n) {
        switch (n) {
            case 8: {
                Helpers.subdivideCubic(fArray, fArray2, fArray3);
                return;
            }
            case 6: {
                Helpers.subdivideQuad(fArray, fArray2, fArray3);
                return;
            }
        }
        throw new InternalError("Unsupported curve type");
    }

    static void isort(float[] fArray, int n) {
        for (int i = 1; i < n; ++i) {
            float f = fArray[i];
            for (int j = i - 1; j >= 0 && fArray[j] > f; --j) {
                fArray[j + 1] = fArray[j];
            }
            fArray[j + 1] = f;
        }
    }

    static void subdivideCubic(float[] fArray, float[] fArray2, float[] fArray3) {
        float f = fArray[0];
        float f2 = fArray[1];
        float f3 = fArray[2];
        float f4 = fArray[3];
        float f5 = fArray[4];
        float f6 = fArray[5];
        float f7 = fArray[6];
        float f8 = fArray[7];
        fArray2[0] = f;
        fArray2[1] = f2;
        fArray3[6] = f7;
        fArray3[7] = f8;
        f = (f + f3) / 2.0f;
        f2 = (f2 + f4) / 2.0f;
        f7 = (f7 + f5) / 2.0f;
        f8 = (f8 + f6) / 2.0f;
        float f9 = (f3 + f5) / 2.0f;
        float f10 = (f4 + f6) / 2.0f;
        f3 = (f + f9) / 2.0f;
        f4 = (f2 + f10) / 2.0f;
        f5 = (f7 + f9) / 2.0f;
        f6 = (f8 + f10) / 2.0f;
        f9 = (f3 + f5) / 2.0f;
        f10 = (f4 + f6) / 2.0f;
        fArray2[2] = f;
        fArray2[3] = f2;
        fArray2[4] = f3;
        fArray2[5] = f4;
        fArray2[6] = f9;
        fArray2[7] = f10;
        fArray3[0] = f9;
        fArray3[1] = f10;
        fArray3[2] = f5;
        fArray3[3] = f6;
        fArray3[4] = f7;
        fArray3[5] = f8;
    }

    static void subdivideCubicAt(float f, float[] fArray, int n, float[] fArray2, int n2, int n3) {
        float f2 = fArray[n];
        float f3 = fArray[n + 1];
        float f4 = fArray[n + 2];
        float f5 = fArray[n + 3];
        float f6 = fArray[n + 4];
        float f7 = fArray[n + 5];
        float f8 = fArray[n + 6];
        float f9 = fArray[n + 7];
        fArray2[n2] = f2;
        fArray2[n2 + 1] = f3;
        fArray2[n3 + 6] = f8;
        fArray2[n3 + 7] = f9;
        f2 += f * (f4 - f2);
        f3 += f * (f5 - f3);
        f8 = f6 + f * (f8 - f6);
        f9 = f7 + f * (f9 - f7);
        float f10 = f4 + f * (f6 - f4);
        float f11 = f5 + f * (f7 - f5);
        f4 = f2 + f * (f10 - f2);
        f5 = f3 + f * (f11 - f3);
        f6 = f10 + f * (f8 - f10);
        f7 = f11 + f * (f9 - f11);
        f10 = f4 + f * (f6 - f4);
        f11 = f5 + f * (f7 - f5);
        fArray2[n2 + 2] = f2;
        fArray2[n2 + 3] = f3;
        fArray2[n2 + 4] = f4;
        fArray2[n2 + 5] = f5;
        fArray2[n2 + 6] = f10;
        fArray2[n2 + 7] = f11;
        fArray2[n3] = f10;
        fArray2[n3 + 1] = f11;
        fArray2[n3 + 2] = f6;
        fArray2[n3 + 3] = f7;
        fArray2[n3 + 4] = f8;
        fArray2[n3 + 5] = f9;
    }

    static void subdivideQuad(float[] fArray, float[] fArray2, float[] fArray3) {
        float f = fArray[0];
        float f2 = fArray[1];
        float f3 = fArray[2];
        float f4 = fArray[3];
        float f5 = fArray[4];
        float f6 = fArray[5];
        fArray2[0] = f;
        fArray2[1] = f2;
        fArray3[4] = f5;
        fArray3[5] = f6;
        f = (f + f3) / 2.0f;
        f2 = (f2 + f4) / 2.0f;
        f5 = (f5 + f3) / 2.0f;
        f6 = (f6 + f4) / 2.0f;
        f3 = (f + f5) / 2.0f;
        f4 = (f2 + f6) / 2.0f;
        fArray2[2] = f;
        fArray2[3] = f2;
        fArray2[4] = f3;
        fArray2[5] = f4;
        fArray3[0] = f3;
        fArray3[1] = f4;
        fArray3[2] = f5;
        fArray3[3] = f6;
    }

    static void subdivideQuadAt(float f, float[] fArray, int n, float[] fArray2, int n2, int n3) {
        float f2 = fArray[n];
        float f3 = fArray[n + 1];
        float f4 = fArray[n + 2];
        float f5 = fArray[n + 3];
        float f6 = fArray[n + 4];
        float f7 = fArray[n + 5];
        fArray2[n2] = f2;
        fArray2[n2 + 1] = f3;
        fArray2[n3 + 4] = f6;
        fArray2[n3 + 5] = f7;
        f2 += f * (f4 - f2);
        f3 += f * (f5 - f3);
        f6 = f4 + f * (f6 - f4);
        f7 = f5 + f * (f7 - f5);
        f4 = f2 + f * (f6 - f2);
        f5 = f3 + f * (f7 - f3);
        fArray2[n2 + 2] = f2;
        fArray2[n2 + 3] = f3;
        fArray2[n2 + 4] = f4;
        fArray2[n2 + 5] = f5;
        fArray2[n3] = f4;
        fArray2[n3 + 1] = f5;
        fArray2[n3 + 2] = f6;
        fArray2[n3 + 3] = f7;
    }

    static void subdivideLineAt(float f, float[] fArray, int n, float[] fArray2, int n2, int n3) {
        float f2 = fArray[n];
        float f3 = fArray[n + 1];
        float f4 = fArray[n + 2];
        float f5 = fArray[n + 3];
        fArray2[n2] = f2;
        fArray2[n2 + 1] = f3;
        fArray2[n3 + 2] = f4;
        fArray2[n3 + 3] = f5;
        f2 += f * (f4 - f2);
        f3 += f * (f5 - f3);
        fArray2[n2 + 2] = f2;
        fArray2[n2 + 3] = f3;
        fArray2[n3] = f2;
        fArray2[n3 + 1] = f3;
    }

    static void subdivideAt(float f, float[] fArray, int n, float[] fArray2, int n2, int n3) {
        if (n3 == 8) {
            Helpers.subdivideCubicAt(f, fArray, n, fArray2, n2, n2 + n3);
        } else if (n3 == 4) {
            Helpers.subdivideLineAt(f, fArray, n, fArray2, n2, n2 + n3);
        } else {
            Helpers.subdivideQuadAt(f, fArray, n, fArray2, n2, n2 + n3);
        }
    }

    static int outcode(float f, float f2, float[] fArray) {
        int n = f2 < fArray[0] ? 1 : (f2 >= fArray[1] ? 2 : 0);
        if (f < fArray[2]) {
            n |= 4;
        } else if (f >= fArray[3]) {
            n |= 8;
        }
        return n;
    }

    static final class IndexStack {
        private static final int INITIAL_COUNT = MarlinConst.INITIAL_EDGES_COUNT >> 2;
        private int end;
        private int[] indices;
        private final IntArrayCache.Reference indices_ref;
        private int indicesUseMark;
        private final StatLong stat_idxstack_indices;
        private final Histogram hist_idxstack_indices;
        private final StatLong stat_array_idxstack_indices;

        IndexStack(RendererContext rendererContext) {
            this(rendererContext, null, null, null);
        }

        IndexStack(RendererContext rendererContext, StatLong statLong, Histogram histogram, StatLong statLong2) {
            this.indices_ref = rendererContext.newDirtyIntArrayRef(INITIAL_COUNT);
            this.indices = this.indices_ref.initial;
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.indicesUseMark = 0;
            }
            this.stat_idxstack_indices = statLong;
            this.hist_idxstack_indices = histogram;
            this.stat_array_idxstack_indices = statLong2;
        }

        void dispose() {
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.stat_idxstack_indices.add(this.indicesUseMark);
                this.hist_idxstack_indices.add(this.indicesUseMark);
                this.indicesUseMark = 0;
            }
            this.indices = this.indices_ref.putArray(this.indices);
        }

        boolean isEmpty() {
            return this.end == 0;
        }

        void reset() {
            this.end = 0;
        }

        void push(int n) {
            int n2;
            int[] nArray = this.indices;
            if ((n2 = this.end--) != 0 && nArray[n2 - 1] == n) {
                return;
            }
            if (nArray.length <= n2) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_idxstack_indices.add(n2 + 1);
                }
                this.indices = nArray = this.indices_ref.widenArray(nArray, n2, n2 + 1);
            }
            nArray[this.end++] = n;
            if (MarlinConst.DO_STATS && this.end > this.indicesUseMark) {
                this.indicesUseMark = this.end;
            }
        }

        void pullAll(float[] fArray, PathConsumer2D pathConsumer2D) {
            int n = this.end;
            if (n == 0) {
                return;
            }
            int[] nArray = this.indices;
            for (int i = 0; i < n; ++i) {
                int n2 = nArray[i] << 1;
                pathConsumer2D.lineTo(fArray[n2], fArray[n2 + 1]);
            }
            this.end = 0;
        }
    }

    static final class PolyStack {
        private static final byte TYPE_LINETO = 0;
        private static final byte TYPE_QUADTO = 1;
        private static final byte TYPE_CUBICTO = 2;
        private static final int INITIAL_CURVES_COUNT = MarlinConst.INITIAL_EDGES_COUNT << 1;
        private static final int INITIAL_TYPES_COUNT = MarlinConst.INITIAL_EDGES_COUNT;
        float[] curves;
        int end;
        byte[] curveTypes;
        int numCurves;
        final FloatArrayCache.Reference curves_ref;
        final ByteArrayCache.Reference curveTypes_ref;
        int curveTypesUseMark;
        int curvesUseMark;
        private final StatLong stat_polystack_types;
        private final StatLong stat_polystack_curves;
        private final Histogram hist_polystack_curves;
        private final StatLong stat_array_polystack_curves;
        private final StatLong stat_array_polystack_curveTypes;

        PolyStack(RendererContext rendererContext) {
            this(rendererContext, null, null, null, null, null);
        }

        PolyStack(RendererContext rendererContext, StatLong statLong, StatLong statLong2, Histogram histogram, StatLong statLong3, StatLong statLong4) {
            this.curves_ref = rendererContext.newDirtyFloatArrayRef(INITIAL_CURVES_COUNT);
            this.curves = this.curves_ref.initial;
            this.curveTypes_ref = rendererContext.newDirtyByteArrayRef(INITIAL_TYPES_COUNT);
            this.curveTypes = this.curveTypes_ref.initial;
            this.numCurves = 0;
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.curveTypesUseMark = 0;
                this.curvesUseMark = 0;
            }
            this.stat_polystack_types = statLong;
            this.stat_polystack_curves = statLong2;
            this.hist_polystack_curves = histogram;
            this.stat_array_polystack_curves = statLong3;
            this.stat_array_polystack_curveTypes = statLong4;
        }

        void dispose() {
            this.end = 0;
            this.numCurves = 0;
            if (MarlinConst.DO_STATS) {
                this.stat_polystack_types.add(this.curveTypesUseMark);
                this.stat_polystack_curves.add(this.curvesUseMark);
                this.hist_polystack_curves.add(this.curvesUseMark);
                this.curveTypesUseMark = 0;
                this.curvesUseMark = 0;
            }
            this.curves = this.curves_ref.putArray(this.curves);
            this.curveTypes = this.curveTypes_ref.putArray(this.curveTypes);
        }

        private void ensureSpace(int n) {
            if (this.curves.length - this.end < n) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_polystack_curves.add(this.end + n);
                }
                this.curves = this.curves_ref.widenArray(this.curves, this.end, this.end + n);
            }
            if (this.curveTypes.length <= this.numCurves) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_polystack_curveTypes.add(this.numCurves + 1);
                }
                this.curveTypes = this.curveTypes_ref.widenArray(this.curveTypes, this.numCurves, this.numCurves + 1);
            }
        }

        void pushCubic(float f, float f2, float f3, float f4, float f5, float f6) {
            this.ensureSpace(6);
            this.curveTypes[this.numCurves++] = 2;
            float[] fArray = this.curves;
            int n = this.end;
            fArray[n++] = f5;
            fArray[n++] = f6;
            fArray[n++] = f3;
            fArray[n++] = f4;
            fArray[n++] = f;
            fArray[n++] = f2;
            this.end = n;
        }

        void pushQuad(float f, float f2, float f3, float f4) {
            this.ensureSpace(4);
            this.curveTypes[this.numCurves++] = 1;
            float[] fArray = this.curves;
            int n = this.end;
            fArray[n++] = f3;
            fArray[n++] = f4;
            fArray[n++] = f;
            fArray[n++] = f2;
            this.end = n;
        }

        void pushLine(float f, float f2) {
            this.ensureSpace(2);
            this.curveTypes[this.numCurves++] = 0;
            this.curves[this.end++] = f;
            this.curves[this.end++] = f2;
        }

        void pullAll(PathConsumer2D pathConsumer2D) {
            int n = this.numCurves;
            if (n == 0) {
                return;
            }
            if (MarlinConst.DO_STATS) {
                if (this.numCurves > this.curveTypesUseMark) {
                    this.curveTypesUseMark = this.numCurves;
                }
                if (this.end > this.curvesUseMark) {
                    this.curvesUseMark = this.end;
                }
            }
            byte[] byArray = this.curveTypes;
            float[] fArray = this.curves;
            int n2 = 0;
            block5: for (int i = 0; i < n; ++i) {
                switch (byArray[i]) {
                    case 0: {
                        pathConsumer2D.lineTo(fArray[n2], fArray[n2 + 1]);
                        n2 += 2;
                        continue block5;
                    }
                    case 2: {
                        pathConsumer2D.curveTo(fArray[n2], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3], fArray[n2 + 4], fArray[n2 + 5]);
                        n2 += 6;
                        continue block5;
                    }
                    case 1: {
                        pathConsumer2D.quadTo(fArray[n2], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3]);
                        n2 += 4;
                        continue block5;
                    }
                }
            }
            this.numCurves = 0;
            this.end = 0;
        }

        void popAll(PathConsumer2D pathConsumer2D) {
            int n = this.numCurves;
            if (n == 0) {
                return;
            }
            if (MarlinConst.DO_STATS) {
                if (this.numCurves > this.curveTypesUseMark) {
                    this.curveTypesUseMark = this.numCurves;
                }
                if (this.end > this.curvesUseMark) {
                    this.curvesUseMark = this.end;
                }
            }
            byte[] byArray = this.curveTypes;
            float[] fArray = this.curves;
            int n2 = this.end;
            block5: while (n != 0) {
                switch (byArray[--n]) {
                    case 0: {
                        pathConsumer2D.lineTo(fArray[n2 -= 2], fArray[n2 + 1]);
                        continue block5;
                    }
                    case 2: {
                        pathConsumer2D.curveTo(fArray[n2 -= 6], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3], fArray[n2 + 4], fArray[n2 + 5]);
                        continue block5;
                    }
                    case 1: {
                        pathConsumer2D.quadTo(fArray[n2 -= 4], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3]);
                        continue block5;
                    }
                }
            }
            this.numCurves = 0;
            this.end = 0;
        }

        public String toString() {
            Object object = "";
            int n = this.numCurves;
            int n2 = this.end;
            while (n != 0) {
                int n3;
                switch (this.curveTypes[--n]) {
                    case 0: {
                        n3 = 2;
                        object = (String)object + "line: ";
                        break;
                    }
                    case 1: {
                        n3 = 4;
                        object = (String)object + "quad: ";
                        break;
                    }
                    case 2: {
                        n3 = 6;
                        object = (String)object + "cubic: ";
                        break;
                    }
                    default: {
                        n3 = 0;
                    }
                }
                object = (String)object + Arrays.toString(Arrays.copyOfRange(this.curves, n2 -= n3, n2 + n3)) + "\n";
            }
            return object;
        }
    }
}

