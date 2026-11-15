/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.marlin.Helpers;

final class Curve {
    float ax;
    float ay;
    float bx;
    float by;
    float cx;
    float cy;
    float dx;
    float dy;
    float dax;
    float day;
    float dbx;
    float dby;

    Curve() {
    }

    void set(float[] fArray, int n) {
        if (n == 8) {
            this.set(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5], fArray[6], fArray[7]);
        } else if (n == 4) {
            this.set(fArray[0], fArray[1], fArray[2], fArray[3]);
        } else {
            this.set(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
        }
    }

    void set(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        float f9 = 3.0f * (f5 - f3);
        float f10 = 3.0f * (f6 - f4);
        float f11 = 3.0f * (f3 - f);
        float f12 = 3.0f * (f4 - f2);
        this.ax = f7 - f - f9;
        this.ay = f8 - f2 - f10;
        this.bx = f9 - f11;
        this.by = f10 - f12;
        this.cx = f11;
        this.cy = f12;
        this.dx = f;
        this.dy = f2;
        this.dax = 3.0f * this.ax;
        this.day = 3.0f * this.ay;
        this.dbx = 2.0f * this.bx;
        this.dby = 2.0f * this.by;
    }

    void set(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = f3 - f;
        float f8 = f4 - f2;
        this.ax = 0.0f;
        this.ay = 0.0f;
        this.bx = f5 - f3 - f7;
        this.by = f6 - f4 - f8;
        this.cx = 2.0f * f7;
        this.cy = 2.0f * f8;
        this.dx = f;
        this.dy = f2;
        this.dax = 0.0f;
        this.day = 0.0f;
        this.dbx = 2.0f * this.bx;
        this.dby = 2.0f * this.by;
    }

    void set(float f, float f2, float f3, float f4) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        this.ax = 0.0f;
        this.ay = 0.0f;
        this.bx = 0.0f;
        this.by = 0.0f;
        this.cx = f5;
        this.cy = f6;
        this.dx = f;
        this.dy = f2;
        this.dax = 0.0f;
        this.day = 0.0f;
        this.dbx = 0.0f;
        this.dby = 0.0f;
    }

    int dxRoots(float[] fArray, int n) {
        return Helpers.quadraticRoots(this.dax, this.dbx, this.cx, fArray, n);
    }

    int dyRoots(float[] fArray, int n) {
        return Helpers.quadraticRoots(this.day, this.dby, this.cy, fArray, n);
    }

    int infPoints(float[] fArray, int n) {
        float f = this.dax * this.dby - this.dbx * this.day;
        float f2 = 2.0f * (this.cy * this.dax - this.day * this.cx);
        float f3 = this.cy * this.dbx - this.cx * this.dby;
        return Helpers.quadraticRoots(f, f2, f3, fArray, n);
    }

    int xPoints(float[] fArray, int n, float f) {
        return Helpers.cubicRootsInAB(this.ax, this.bx, this.cx, this.dx - f, fArray, n, 0.0f, 1.0f);
    }

    int yPoints(float[] fArray, int n, float f) {
        return Helpers.cubicRootsInAB(this.ay, this.by, this.cy, this.dy - f, fArray, n, 0.0f, 1.0f);
    }

    private int perpendiculardfddf(float[] fArray, int n) {
        assert (fArray.length >= n + 4);
        float f = 2.0f * (this.dax * this.dax + this.day * this.day);
        float f2 = 3.0f * (this.dax * this.dbx + this.day * this.dby);
        float f3 = 2.0f * (this.dax * this.cx + this.day * this.cy) + this.dbx * this.dbx + this.dby * this.dby;
        float f4 = this.dbx * this.cx + this.dby * this.cy;
        return Helpers.cubicRootsInAB(f, f2, f3, f4, fArray, n, 0.0f, 1.0f);
    }

    int rootsOfROCMinusW(float[] fArray, int n, float f, float f2) {
        assert (n <= 6 && fArray.length >= 10);
        int n2 = n;
        int n3 = n + this.perpendiculardfddf(fArray, n);
        fArray[n3] = 1.0f;
        float f3 = 0.0f;
        float f4 = this.ROCsq(f3) - f;
        for (int i = n; i <= n3; ++i) {
            float f5 = fArray[i];
            float f6 = this.ROCsq(f5) - f;
            if (f4 == 0.0f) {
                fArray[n2++] = f3;
            } else if (f6 * f4 < 0.0f) {
                fArray[n2++] = this.falsePositionROCsqMinusX(f3, f5, f, f2);
            }
            f3 = f5;
            f4 = f6;
        }
        return n2 - n;
    }

    private static float eliminateInf(float f) {
        return f == Float.POSITIVE_INFINITY ? Float.MAX_VALUE : (f == Float.NEGATIVE_INFINITY ? Float.MIN_VALUE : f);
    }

    private float falsePositionROCsqMinusX(float f, float f2, float f3, float f4) {
        int n = 0;
        float f5 = f2;
        float f6 = Curve.eliminateInf(this.ROCsq(f5) - f3);
        float f7 = f;
        float f8 = Curve.eliminateInf(this.ROCsq(f7) - f3);
        float f9 = f7;
        for (int i = 0; i < 100 && Math.abs(f5 - f7) > f4 * Math.abs(f5 + f7); ++i) {
            f9 = (f8 * f5 - f6 * f7) / (f8 - f6);
            float f10 = this.ROCsq(f9) - f3;
            if (Curve.sameSign(f10, f6)) {
                f6 = f10;
                f5 = f9;
                if (n < 0) {
                    f8 /= (float)(1 << -n);
                    --n;
                    continue;
                }
                n = -1;
                continue;
            }
            if (!(f10 * f8 > 0.0f)) break;
            f8 = f10;
            f7 = f9;
            if (n > 0) {
                f6 /= (float)(1 << n);
                ++n;
                continue;
            }
            n = 1;
        }
        return f9;
    }

    private static boolean sameSign(float f, float f2) {
        return f < 0.0f && f2 < 0.0f || f > 0.0f && f2 > 0.0f;
    }

    private float ROCsq(float f) {
        float f2 = f * (f * this.dax + this.dbx) + this.cx;
        float f3 = f * (f * this.day + this.dby) + this.cy;
        float f4 = 2.0f * this.dax * f + this.dbx;
        float f5 = 2.0f * this.day * f + this.dby;
        float f6 = f2 * f2 + f3 * f3;
        float f7 = f4 * f4 + f5 * f5;
        float f8 = f4 * f2 + f5 * f3;
        return f6 * (f6 * f6 / (f6 * f7 - f8 * f8));
    }
}

