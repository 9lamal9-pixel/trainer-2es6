/*
 * Decompiled with CFR 0.152.
 */
package com.sun.pisces;

public final class GradientColorMap {
    public static final int CYCLE_NONE = 0;
    public static final int CYCLE_REPEAT = 1;
    public static final int CYCLE_REFLECT = 2;
    int cycleMethod;
    private static final int LG_RAMP_SIZE = 8;
    private static final int RAMP_SIZE = 256;
    int[] fractions = null;
    int[] rgba = null;
    int[] colors = null;

    GradientColorMap(int[] nArray, int[] nArray2, int n) {
        int[] nArray3;
        int[] nArray4;
        this.cycleMethod = n;
        int n2 = nArray.length;
        if (nArray[0] != 0) {
            nArray4 = new int[n2 + 1];
            nArray3 = new int[n2 + 1];
            System.arraycopy(nArray, 0, nArray4, 1, n2);
            System.arraycopy(nArray2, 0, nArray3, 1, n2);
            nArray4[0] = 0;
            nArray3[0] = nArray2[0];
            nArray = nArray4;
            nArray2 = nArray3;
            ++n2;
        }
        if (nArray[n2 - 1] != 65536) {
            nArray4 = new int[n2 + 1];
            nArray3 = new int[n2 + 1];
            System.arraycopy(nArray, 0, nArray4, 0, n2);
            System.arraycopy(nArray2, 0, nArray3, 0, n2);
            nArray4[n2] = 65536;
            nArray3[n2] = nArray2[n2 - 1];
            nArray = nArray4;
            nArray2 = nArray3;
        }
        this.fractions = new int[nArray.length];
        System.arraycopy(nArray, 0, this.fractions, 0, nArray.length);
        this.rgba = new int[nArray2.length];
        System.arraycopy(nArray2, 0, this.rgba, 0, nArray2.length);
        this.createRamp();
    }

    private int pad(int n) {
        switch (this.cycleMethod) {
            case 0: {
                if (n < 0) {
                    return 0;
                }
                if (n > 65535) {
                    return 65535;
                }
                return n;
            }
            case 1: {
                return n & 0xFFFF;
            }
            case 2: {
                if (n < 0) {
                    n = -n;
                }
                if ((n &= 0x1FFFF) > 65535) {
                    n = 131071 - n;
                }
                return n;
            }
        }
        throw new RuntimeException("Unknown cycle method: " + this.cycleMethod);
    }

    private int findStop(int n) {
        int n2 = this.fractions.length;
        for (int i = 1; i < n2; ++i) {
            if (this.fractions[i] <= n) continue;
            return i;
        }
        return 1;
    }

    private void accumColor(int n, int[] nArray, int[] nArray2, int[] nArray3, int[] nArray4, int[] nArray5, int[] nArray6, int[] nArray7, int[] nArray8) {
        int n2 = this.findStop(n);
        int n3 = this.fractions[n2] - this.fractions[n2 - 1];
        nArray5[0] = nArray5[0] + (nArray[n2 - 1] + (n -= this.fractions[n2 - 1]) * (nArray[n2] - nArray[n2 - 1]) / n3);
        nArray6[0] = nArray6[0] + (nArray2[n2 - 1] + n * (nArray2[n2] - nArray2[n2 - 1]) / n3);
        nArray7[0] = nArray7[0] + (nArray3[n2 - 1] + n * (nArray3[n2] - nArray3[n2 - 1]) / n3);
        nArray8[0] = nArray8[0] + (nArray4[n2 - 1] + n * (nArray4[n2] - nArray4[n2 - 1]) / n3);
    }

    private int getColorAA(int n, int[] nArray, int[] nArray2, int[] nArray3, int[] nArray4, int[] nArray5, int[] nArray6, int[] nArray7, int[] nArray8) {
        int n2;
        int n3 = this.findStop(n);
        if (this.fractions[n3 - 1] < this.pad(n - (n2 = 192)) && this.pad(n + n2) < this.fractions[n3]) {
            n2 = 0;
        }
        int n4 = 64;
        int n5 = 0;
        for (int i = -n2; i <= n2; i += n4) {
            int n6 = this.pad(n + i);
            this.accumColor(n6, nArray, nArray2, nArray3, nArray4, nArray5, nArray6, nArray7, nArray8);
            ++n5;
        }
        nArray8[0] = nArray8[0] / n5;
        nArray5[0] = nArray5[0] / n5;
        nArray6[0] = nArray6[0] / n5;
        nArray7[0] = nArray7[0] / n5;
        return nArray8[0] << 24 | nArray5[0] << 16 | nArray6[0] << 8 | nArray7[0];
    }

    private void createRamp() {
        int n;
        this.colors = new int[256];
        int[] nArray = new int[1];
        int[] nArray2 = new int[1];
        int[] nArray3 = new int[1];
        int[] nArray4 = new int[1];
        int n2 = this.fractions.length;
        int[] nArray5 = new int[n2];
        int[] nArray6 = new int[n2];
        int[] nArray7 = new int[n2];
        int[] nArray8 = new int[n2];
        for (n = 0; n < n2; ++n) {
            nArray5[n] = this.rgba[n] >> 24 & 0xFF;
            nArray6[n] = this.rgba[n] >> 16 & 0xFF;
            nArray7[n] = this.rgba[n] >> 8 & 0xFF;
            nArray8[n] = this.rgba[n] & 0xFF;
        }
        n = 255;
        int n3 = 8;
        this.colors[0] = this.rgba[0];
        this.colors[n] = this.rgba[n2 - 1];
        for (int i = 1; i < n; ++i) {
            nArray[0] = 0;
            nArray4[0] = 0;
            nArray3[0] = 0;
            nArray2[0] = 0;
            this.colors[i] = this.getColorAA(i << n3, nArray6, nArray7, nArray8, nArray5, nArray2, nArray3, nArray4, nArray);
        }
    }
}

