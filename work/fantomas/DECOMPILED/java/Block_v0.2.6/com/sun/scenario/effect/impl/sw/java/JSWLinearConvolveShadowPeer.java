/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.java;

import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.LinearConvolveRenderState;
import com.sun.scenario.effect.impl.sw.java.JSWLinearConvolvePeer;

public class JSWLinearConvolveShadowPeer
extends JSWLinearConvolvePeer {
    public JSWLinearConvolveShadowPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    private float[] getShadowColor() {
        return ((LinearConvolveRenderState)this.getRenderState()).getPassShadowColorComponents();
    }

    @Override
    protected void filterVector(int[] nArray, int n, int n2, int n3, int[] nArray2, int n4, int n5, int n6, float[] fArray, int n7, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        float[] fArray2 = this.getShadowColor();
        int n8 = 0;
        f += (f9 + f7) * 0.5f;
        f2 += (f10 + f8) * 0.5f;
        for (int i = 0; i < n2; ++i) {
            float f11 = f;
            float f12 = f2;
            for (int j = 0; j < n; ++j) {
                float f13 = 0.0f;
                float f14 = f11 + f3;
                float f15 = f12 + f4;
                for (int k = 0; k < n7; ++k) {
                    if (f14 >= 0.0f && f15 >= 0.0f) {
                        int n9 = (int)f14;
                        int n10 = (int)f15;
                        if (n9 < n4 && n10 < n5) {
                            int n11 = nArray2[n10 * n6 + n9];
                            f13 += (float)(n11 >>> 24) * fArray[k];
                        }
                    }
                    f14 += f5;
                    f15 += f6;
                }
                f13 = f13 < 0.0f ? 0.0f : (f13 > 255.0f ? 255.0f : f13);
                nArray[n8 + j] = (int)(fArray2[0] * f13) << 16 | (int)(fArray2[1] * f13) << 8 | (int)(fArray2[2] * f13) | (int)(fArray2[3] * f13) << 24;
                f11 += f7;
                f12 += f8;
            }
            f += f9;
            f2 += f10;
            n8 += n3;
        }
    }

    @Override
    protected void filterHV(int[] nArray, int n, int n2, int n3, int n4, int[] nArray2, int n5, int n6, int n7, int n8, float[] fArray) {
        int n9;
        float[] fArray2 = this.getShadowColor();
        int n10 = fArray.length / 2;
        float[] fArray3 = new float[n10];
        int n11 = 0;
        int n12 = 0;
        int[] nArray3 = new int[256];
        for (n9 = 0; n9 < nArray3.length; ++n9) {
            nArray3[n9] = (int)(fArray2[0] * (float)n9) << 16 | (int)(fArray2[1] * (float)n9) << 8 | (int)(fArray2[2] * (float)n9) | (int)(fArray2[3] * (float)n9) << 24;
        }
        for (n9 = 0; n9 < n2; ++n9) {
            int n13;
            int n14 = n11;
            int n15 = n12;
            for (n13 = 0; n13 < fArray3.length; ++n13) {
                fArray3[n13] = 0.0f;
            }
            n13 = n10;
            for (int i = 0; i < n; ++i) {
                fArray3[n10 - n13] = (i < n5 ? nArray2[n15] : 0) >>> 24;
                if (--n13 <= 0) {
                    n13 += n10;
                }
                float f = -0.5f;
                for (int j = 0; j < fArray3.length; ++j) {
                    f += fArray3[j] * fArray[n13 + j];
                }
                nArray[n14] = f < 0.0f ? 0 : (f >= 254.0f ? nArray3[255] : nArray3[(int)f + 1]);
                n14 += n3;
                n15 += n7;
            }
            n11 += n4;
            n12 += n8;
        }
    }
}

