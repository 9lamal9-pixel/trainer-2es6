/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import com.sun.webkit.graphics.Ref;
import java.util.Arrays;

public final class WCTransform
extends Ref {
    private final double[] m;
    private final boolean is3D;

    public WCTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12, double d13, double d14, double d15, double d16) {
        this.m = new double[16];
        this.m[0] = d;
        this.m[1] = d5;
        this.m[2] = d9;
        this.m[3] = d13;
        this.m[4] = d2;
        this.m[5] = d6;
        this.m[6] = d10;
        this.m[7] = d14;
        this.m[8] = d3;
        this.m[9] = d7;
        this.m[10] = d11;
        this.m[11] = d15;
        this.m[12] = d4;
        this.m[13] = d8;
        this.m[14] = d12;
        this.m[15] = d16;
        this.is3D = true;
    }

    public WCTransform(double d, double d2, double d3, double d4, double d5, double d6) {
        this.m = new double[6];
        this.m[0] = d;
        this.m[1] = d2;
        this.m[2] = d3;
        this.m[3] = d4;
        this.m[4] = d5;
        this.m[5] = d6;
        this.is3D = false;
    }

    public double[] getMatrix() {
        return Arrays.copyOf(this.m, this.m.length);
    }

    public String toString() {
        Object object = "WCTransform:";
        object = this.is3D ? (String)object + "(" + this.m[0] + "," + this.m[1] + "," + this.m[2] + "," + this.m[3] + ")(" + this.m[4] + "," + this.m[5] + "," + this.m[6] + "," + this.m[7] + ")(" + this.m[8] + "," + this.m[9] + "," + this.m[10] + "," + this.m[11] + ")(" + this.m[12] + "," + this.m[13] + "," + this.m[14] + "," + this.m[15] + ")" : (String)object + "(" + this.m[0] + "," + this.m[1] + "," + this.m[2] + ")(" + this.m[3] + "," + this.m[4] + "," + this.m[5] + ")";
        return object;
    }
}

