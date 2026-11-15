/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.java;

import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.RenderState;

public abstract class JSWEffectPeer<T extends RenderState>
extends EffectPeer<T> {
    protected static final int FVALS_A = 3;
    protected static final int FVALS_R = 0;
    protected static final int FVALS_G = 1;
    protected static final int FVALS_B = 2;

    protected JSWEffectPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    protected final void laccum(int n, float f, float[] fArray) {
        fArray[0] = fArray[0] + (float)(n >> 16 & 0xFF) * (f /= 255.0f);
        fArray[1] = fArray[1] + (float)(n >> 8 & 0xFF) * f;
        fArray[2] = fArray[2] + (float)(n & 0xFF) * f;
        fArray[3] = fArray[3] + (float)(n >>> 24) * f;
    }

    protected final void lsample(int[] nArray, float f, float f2, int n, int n2, int n3, float[] fArray) {
        fArray[0] = 0.0f;
        fArray[1] = 0.0f;
        fArray[2] = 0.0f;
        fArray[3] = 0.0f;
        f = f * (float)n + 0.5f;
        f2 = f2 * (float)n2 + 0.5f;
        int n4 = (int)f;
        int n5 = (int)f2;
        if (f > 0.0f && f2 > 0.0f && n4 <= n && n5 <= n2) {
            int n6 = n5 * n3 + n4;
            float f3 = (f -= (float)n4) * (f2 -= (float)n5);
            if (n5 < n2) {
                if (n4 < n) {
                    this.laccum(nArray[n6], f3, fArray);
                }
                if (n4 > 0) {
                    this.laccum(nArray[n6 - 1], f2 - f3, fArray);
                }
            }
            if (n5 > 0) {
                if (n4 < n) {
                    this.laccum(nArray[n6 - n3], f - f3, fArray);
                }
                if (n4 > 0) {
                    this.laccum(nArray[n6 - n3 - 1], 1.0f - f - f2 + f3, fArray);
                }
            }
        }
    }

    protected final void laccumsample(int[] nArray, float f, float f2, int n, int n2, int n3, float f3, float[] fArray) {
        f3 *= 255.0f;
        int n4 = (int)(f += 0.5f);
        int n5 = (int)(f2 += 0.5f);
        if (f > 0.0f && f2 > 0.0f && n4 <= n && n5 <= n2) {
            int n6 = n5 * n3 + n4;
            float f4 = (f -= (float)n4) * (f2 -= (float)n5);
            if (n5 < n2) {
                if (n4 < n) {
                    this.laccum(nArray[n6], f4 * f3, fArray);
                }
                if (n4 > 0) {
                    this.laccum(nArray[n6 - 1], (f2 - f4) * f3, fArray);
                }
            }
            if (n5 > 0) {
                if (n4 < n) {
                    this.laccum(nArray[n6 - n3], (f - f4) * f3, fArray);
                }
                if (n4 > 0) {
                    this.laccum(nArray[n6 - n3 - 1], (1.0f - f - f2 + f4) * f3, fArray);
                }
            }
        }
    }

    protected final void faccum(float[] fArray, int n, float f, float[] fArray2) {
        fArray2[0] = fArray2[0] + fArray[n] * f;
        fArray2[1] = fArray2[1] + fArray[n + 1] * f;
        fArray2[2] = fArray2[2] + fArray[n + 2] * f;
        fArray2[3] = fArray2[3] + fArray[n + 3] * f;
    }

    protected final void fsample(float[] fArray, float f, float f2, int n, int n2, int n3, float[] fArray2) {
        fArray2[0] = 0.0f;
        fArray2[1] = 0.0f;
        fArray2[2] = 0.0f;
        fArray2[3] = 0.0f;
        f = f * (float)n + 0.5f;
        f2 = f2 * (float)n2 + 0.5f;
        int n4 = (int)f;
        int n5 = (int)f2;
        if (f > 0.0f && f2 > 0.0f && n4 <= n && n5 <= n2) {
            int n6 = 4 * (n5 * n3 + n4);
            float f3 = (f -= (float)n4) * (f2 -= (float)n5);
            if (n5 < n2) {
                if (n4 < n) {
                    this.faccum(fArray, n6, f3, fArray2);
                }
                if (n4 > 0) {
                    this.faccum(fArray, n6 - 4, f2 - f3, fArray2);
                }
            }
            if (n5 > 0) {
                if (n4 < n) {
                    this.faccum(fArray, n6 - n3 * 4, f - f3, fArray2);
                }
                if (n4 > 0) {
                    this.faccum(fArray, n6 - n3 * 4 - 4, 1.0f - f - f2 + f3, fArray2);
                }
            }
        }
    }
}

