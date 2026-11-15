/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.geom;

import com.sun.javafx.geom.CubicCurve2D;
import com.sun.javafx.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CubicApproximator {
    private float accuracy;
    private float maxCubicSize;

    public CubicApproximator(float f, float f2) {
        this.accuracy = f;
        this.maxCubicSize = f2;
    }

    public void setAccuracy(float f) {
        this.accuracy = f;
    }

    public float getAccuracy() {
        return this.accuracy;
    }

    public void setMaxCubicSize(float f) {
        this.maxCubicSize = f;
    }

    public float getMaxCubicSize() {
        return this.maxCubicSize;
    }

    public float approximate(List<QuadCurve2D> list, List<CubicCurve2D> list2, CubicCurve2D cubicCurve2D) {
        float f = CubicApproximator.getApproxError(cubicCurve2D);
        if (f < this.accuracy) {
            list2.add(cubicCurve2D);
            list.add(this.approximate(cubicCurve2D));
            return f;
        }
        this.SplitCubic(list2, new float[]{cubicCurve2D.x1, cubicCurve2D.y1, cubicCurve2D.ctrlx1, cubicCurve2D.ctrly1, cubicCurve2D.ctrlx2, cubicCurve2D.ctrly2, cubicCurve2D.x2, cubicCurve2D.y2});
        return this.approximate(list2, list);
    }

    public float approximate(List<QuadCurve2D> list, CubicCurve2D cubicCurve2D) {
        ArrayList<CubicCurve2D> arrayList = new ArrayList<CubicCurve2D>();
        return this.approximate(list, arrayList, cubicCurve2D);
    }

    private QuadCurve2D approximate(CubicCurve2D cubicCurve2D) {
        return new QuadCurve2D(cubicCurve2D.x1, cubicCurve2D.y1, (3.0f * cubicCurve2D.ctrlx1 - cubicCurve2D.x1 + 3.0f * cubicCurve2D.ctrlx2 - cubicCurve2D.x2) / 4.0f, (3.0f * cubicCurve2D.ctrly1 - cubicCurve2D.y1 + 3.0f * cubicCurve2D.ctrly2 - cubicCurve2D.y2) / 4.0f, cubicCurve2D.x2, cubicCurve2D.y2);
    }

    private float approximate(List<CubicCurve2D> list, List<QuadCurve2D> list2) {
        QuadCurve2D quadCurve2D = this.approximate(list.get(0));
        float f = CubicApproximator.compareCPs(list.get(0), CubicApproximator.elevate(quadCurve2D));
        list2.add(quadCurve2D);
        for (int i = 1; i < list.size(); ++i) {
            quadCurve2D = this.approximate(list.get(i));
            float f2 = CubicApproximator.compareCPs(list.get(i), CubicApproximator.elevate(quadCurve2D));
            if (f2 > f) {
                f = f2;
            }
            list2.add(quadCurve2D);
        }
        return f;
    }

    private static CubicCurve2D elevate(QuadCurve2D quadCurve2D) {
        return new CubicCurve2D(quadCurve2D.x1, quadCurve2D.y1, (quadCurve2D.x1 + 2.0f * quadCurve2D.ctrlx) / 3.0f, (quadCurve2D.y1 + 2.0f * quadCurve2D.ctrly) / 3.0f, (2.0f * quadCurve2D.ctrlx + quadCurve2D.x2) / 3.0f, (2.0f * quadCurve2D.ctrly + quadCurve2D.y2) / 3.0f, quadCurve2D.x2, quadCurve2D.y2);
    }

    private static float compare(CubicCurve2D cubicCurve2D, CubicCurve2D cubicCurve2D2) {
        float f;
        float f2 = Math.abs(cubicCurve2D.x1 - cubicCurve2D2.x1);
        if (f2 < (f = Math.abs(cubicCurve2D.y1 - cubicCurve2D2.y1))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrlx1 - cubicCurve2D2.ctrlx1))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrly1 - cubicCurve2D2.ctrly1))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrlx2 - cubicCurve2D2.ctrlx2))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrly2 - cubicCurve2D2.ctrly2))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.x2 - cubicCurve2D2.x2))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.y2 - cubicCurve2D2.y2))) {
            f2 = f;
        }
        return f2;
    }

    private static float getApproxError(float[] fArray) {
        float f = (-3.0f * fArray[2] + fArray[0] + 3.0f * fArray[4] - fArray[6]) / 6.0f;
        float f2 = (-3.0f * fArray[3] + fArray[1] + 3.0f * fArray[5] - fArray[7]) / 6.0f;
        if (f < f2) {
            f = f2;
        }
        if (f < (f2 = (3.0f * fArray[2] - fArray[0] - 3.0f * fArray[4] + fArray[6]) / 6.0f)) {
            f = f2;
        }
        if (f < (f2 = (3.0f * fArray[3] - fArray[1] - 3.0f * fArray[5] + fArray[7]) / 6.0f)) {
            f = f2;
        }
        return f;
    }

    public static float getApproxError(CubicCurve2D cubicCurve2D) {
        return CubicApproximator.getApproxError(new float[]{cubicCurve2D.x1, cubicCurve2D.y1, cubicCurve2D.ctrlx1, cubicCurve2D.ctrly1, cubicCurve2D.ctrlx2, cubicCurve2D.ctrly2, cubicCurve2D.x2, cubicCurve2D.y2});
    }

    private static float compareCPs(CubicCurve2D cubicCurve2D, CubicCurve2D cubicCurve2D2) {
        float f;
        float f2 = Math.abs(cubicCurve2D.ctrlx1 - cubicCurve2D2.ctrlx1);
        if (f2 < (f = Math.abs(cubicCurve2D.ctrly1 - cubicCurve2D2.ctrly1))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrlx2 - cubicCurve2D2.ctrlx2))) {
            f2 = f;
        }
        if (f2 < (f = Math.abs(cubicCurve2D.ctrly2 - cubicCurve2D2.ctrly2))) {
            f2 = f;
        }
        return f2;
    }

    private void ProcessMonotonicCubic(List<CubicCurve2D> list, float[] fArray) {
        float f;
        float f2;
        float[] fArray2 = new float[8];
        float f3 = f2 = fArray[0];
        float f4 = f = fArray[1];
        for (int i = 2; i < 8; i += 2) {
            f3 = f3 > fArray[i] ? fArray[i] : f3;
            f2 = f2 < fArray[i] ? fArray[i] : f2;
            f4 = f4 > fArray[i + 1] ? fArray[i + 1] : f4;
            f = f < fArray[i + 1] ? fArray[i + 1] : f;
        }
        if (f2 - f3 > this.maxCubicSize || f - f4 > this.maxCubicSize || CubicApproximator.getApproxError(fArray) > this.accuracy) {
            fArray2[6] = fArray[6];
            fArray2[7] = fArray[7];
            fArray2[4] = (fArray[4] + fArray[6]) / 2.0f;
            fArray2[5] = (fArray[5] + fArray[7]) / 2.0f;
            float f5 = (fArray[2] + fArray[4]) / 2.0f;
            float f6 = (fArray[3] + fArray[5]) / 2.0f;
            fArray2[2] = (f5 + fArray2[4]) / 2.0f;
            fArray2[3] = (f6 + fArray2[5]) / 2.0f;
            fArray[2] = (fArray[0] + fArray[2]) / 2.0f;
            fArray[3] = (fArray[1] + fArray[3]) / 2.0f;
            fArray[4] = (fArray[2] + f5) / 2.0f;
            fArray[5] = (fArray[3] + f6) / 2.0f;
            fArray[6] = fArray2[0] = (fArray[4] + fArray2[2]) / 2.0f;
            fArray[7] = fArray2[1] = (fArray[5] + fArray2[3]) / 2.0f;
            this.ProcessMonotonicCubic(list, fArray);
            this.ProcessMonotonicCubic(list, fArray2);
        } else {
            list.add(new CubicCurve2D(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5], fArray[6], fArray[7]));
        }
    }

    public void SplitCubic(List<CubicCurve2D> list, float[] fArray) {
        int n;
        int n2;
        float[] fArray2 = new float[4];
        float[] fArray3 = new float[3];
        float[] fArray4 = new float[2];
        int n3 = 0;
        if ((fArray[0] > fArray[2] || fArray[2] > fArray[4] || fArray[4] > fArray[6]) && (fArray[0] < fArray[2] || fArray[2] < fArray[4] || fArray[4] < fArray[6])) {
            fArray3[2] = -fArray[0] + 3.0f * fArray[2] - 3.0f * fArray[4] + fArray[6];
            fArray3[1] = 2.0f * (fArray[0] - 2.0f * fArray[2] + fArray[4]);
            fArray3[0] = -fArray[0] + fArray[2];
            n2 = QuadCurve2D.solveQuadratic(fArray3, fArray4);
            for (n = 0; n < n2; ++n) {
                if (!(fArray4[n] > 0.0f) || !(fArray4[n] < 1.0f)) continue;
                fArray2[n3++] = fArray4[n];
            }
        }
        if ((fArray[1] > fArray[3] || fArray[3] > fArray[5] || fArray[5] > fArray[7]) && (fArray[1] < fArray[3] || fArray[3] < fArray[5] || fArray[5] < fArray[7])) {
            fArray3[2] = -fArray[1] + 3.0f * fArray[3] - 3.0f * fArray[5] + fArray[7];
            fArray3[1] = 2.0f * (fArray[1] - 2.0f * fArray[3] + fArray[5]);
            fArray3[0] = -fArray[1] + fArray[3];
            n2 = QuadCurve2D.solveQuadratic(fArray3, fArray4);
            for (n = 0; n < n2; ++n) {
                if (!(fArray4[n] > 0.0f) || !(fArray4[n] < 1.0f)) continue;
                fArray2[n3++] = fArray4[n];
            }
        }
        if (n3 > 0) {
            Arrays.sort(fArray2, 0, n3);
            this.ProcessFirstMonotonicPartOfCubic(list, fArray, fArray2[0]);
            for (n2 = 1; n2 < n3; ++n2) {
                float f = fArray2[n2] - fArray2[n2 - 1];
                if (!(f > 0.0f)) continue;
                this.ProcessFirstMonotonicPartOfCubic(list, fArray, f / (1.0f - fArray2[n2 - 1]));
            }
        }
        this.ProcessMonotonicCubic(list, fArray);
    }

    private void ProcessFirstMonotonicPartOfCubic(List<CubicCurve2D> list, float[] fArray, float f) {
        float[] fArray2 = new float[8];
        fArray2[0] = fArray[0];
        fArray2[1] = fArray[1];
        float f2 = fArray[2] + f * (fArray[4] - fArray[2]);
        float f3 = fArray[3] + f * (fArray[5] - fArray[3]);
        fArray2[2] = fArray[0] + f * (fArray[2] - fArray[0]);
        fArray2[3] = fArray[1] + f * (fArray[3] - fArray[1]);
        fArray2[4] = fArray2[2] + f * (f2 - fArray2[2]);
        fArray2[5] = fArray2[3] + f * (f3 - fArray2[3]);
        fArray[4] = fArray[4] + f * (fArray[6] - fArray[4]);
        fArray[5] = fArray[5] + f * (fArray[7] - fArray[5]);
        fArray[2] = f2 + f * (fArray[4] - f2);
        fArray[3] = f3 + f * (fArray[5] - f3);
        fArray[0] = fArray2[6] = fArray2[4] + f * (fArray[2] - fArray2[4]);
        fArray[1] = fArray2[7] = fArray2[5] + f * (fArray[3] - fArray2[5]);
        this.ProcessMonotonicCubic(list, fArray2);
    }
}

