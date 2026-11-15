/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.java;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.InvertMask;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.RenderState;
import com.sun.scenario.effect.impl.sw.java.JSWEffectPeer;

public class JSWInvertMaskPeer
extends JSWEffectPeer {
    public JSWInvertMaskPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    protected final InvertMask getEffect() {
        return (InvertMask)super.getEffect();
    }

    private float[] getOffset() {
        float f = this.getEffect().getOffsetX();
        float f2 = this.getEffect().getOffsetY();
        float[] fArray = new float[]{f, f2};
        try {
            this.getInputTransform(0).inverseDeltaTransform(fArray, 0, fArray, 0, 1);
        }
        catch (Exception exception) {
            // empty catch block
        }
        fArray[0] = fArray[0] / (float)this.getInputNativeBounds((int)0).width;
        fArray[1] = fArray[1] / (float)this.getInputNativeBounds((int)0).height;
        return fArray;
    }

    @Override
    public ImageData filter(Effect effect, RenderState renderState, BaseTransform baseTransform, Rectangle rectangle, ImageData ... imageDataArray) {
        this.setEffect(effect);
        Rectangle rectangle2 = this.getResultBounds(baseTransform, rectangle, imageDataArray);
        this.setDestBounds(rectangle2);
        HeapImage heapImage = (HeapImage)imageDataArray[0].getTransformedImage(rectangle2);
        int n = 0;
        int n2 = 0;
        int n3 = heapImage.getPhysicalWidth();
        int n4 = heapImage.getPhysicalHeight();
        int n5 = heapImage.getScanlineStride();
        int[] nArray = heapImage.getPixelArray();
        Rectangle rectangle3 = new Rectangle(n, n2, n3, n4);
        Rectangle rectangle4 = imageDataArray[0].getTransformedBounds(rectangle2);
        BaseTransform baseTransform2 = BaseTransform.IDENTITY_TRANSFORM;
        this.setInputBounds(0, rectangle4);
        this.setInputNativeBounds(0, rectangle3);
        float[] fArray = new float[4];
        this.getTextureCoordinates(0, fArray, rectangle4.x, rectangle4.y, n3, n4, rectangle2, baseTransform2);
        int n6 = rectangle2.width;
        int n7 = rectangle2.height;
        HeapImage heapImage2 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n6, n7));
        this.setDestNativeBounds(heapImage2.getPhysicalWidth(), heapImage2.getPhysicalHeight());
        int n8 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        float[] fArray2 = this.getOffset();
        float f = fArray2[0];
        float f2 = fArray2[1];
        float f3 = (fArray[2] - fArray[0]) / (float)n6;
        float f4 = (fArray[3] - fArray[1]) / (float)n7;
        float f5 = fArray[1] + f4 * 0.5f;
        for (int i = 0; i < 0 + n7; ++i) {
            float f6 = i;
            int n9 = i * n8;
            float f7 = fArray[0] + f3 * 0.5f;
            for (int j = 0; j < 0 + n6; ++j) {
                float f8;
                int n10;
                float f9 = j;
                float f10 = f7 - f;
                float f11 = f5 - f2;
                if (f10 >= 0.0f && f11 >= 0.0f) {
                    int n11 = (int)(f10 * (float)n3);
                    int n12 = (int)(f11 * (float)n4);
                    boolean bl = n11 >= n3 || n12 >= n4;
                    n10 = bl ? 0 : nArray[n12 * n5 + n11];
                } else {
                    n10 = 0;
                }
                f10 = f8 = (float)(n10 >>> 24) / 255.0f;
                float f12 = f11 = 1.0f - f10;
                float f13 = f11;
                float f14 = f11;
                float f15 = f11;
                if (f15 < 0.0f) {
                    f15 = 0.0f;
                } else if (f15 > 1.0f) {
                    f15 = 1.0f;
                }
                if (f12 < 0.0f) {
                    f12 = 0.0f;
                } else if (f12 > f15) {
                    f12 = f15;
                }
                if (f13 < 0.0f) {
                    f13 = 0.0f;
                } else if (f13 > f15) {
                    f13 = f15;
                }
                if (f14 < 0.0f) {
                    f14 = 0.0f;
                } else if (f14 > f15) {
                    f14 = f15;
                }
                nArray2[n9 + j] = (int)(f12 * 255.0f) << 16 | (int)(f13 * 255.0f) << 8 | (int)(f14 * 255.0f) << 0 | (int)(f15 * 255.0f) << 24;
                f7 += f3;
            }
            f5 += f4;
        }
        imageDataArray[0].releaseTransformedImage(heapImage);
        return new ImageData(this.getFilterContext(), heapImage2, rectangle2);
    }
}

