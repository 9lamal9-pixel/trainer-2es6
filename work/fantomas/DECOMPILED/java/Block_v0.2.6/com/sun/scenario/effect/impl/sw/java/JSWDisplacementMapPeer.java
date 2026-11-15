/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.java;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.DisplacementMap;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.FloatMap;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.RenderState;
import com.sun.scenario.effect.impl.sw.java.JSWEffectPeer;

public class JSWDisplacementMapPeer
extends JSWEffectPeer {
    public JSWDisplacementMapPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    protected final DisplacementMap getEffect() {
        return (DisplacementMap)super.getEffect();
    }

    private float[] getSampletx() {
        return new float[]{this.getEffect().getOffsetX(), this.getEffect().getOffsetY(), this.getEffect().getScaleX(), this.getEffect().getScaleY()};
    }

    private float[] getImagetx() {
        float f = this.getEffect().getWrap() ? 0.5f : 0.0f;
        return new float[]{f / (float)this.getInputNativeBounds((int)0).width, f / (float)this.getInputNativeBounds((int)0).height, ((float)this.getInputBounds((int)0).width - 2.0f * f) / (float)this.getInputNativeBounds((int)0).width, ((float)this.getInputBounds((int)0).height - 2.0f * f) / (float)this.getInputNativeBounds((int)0).height};
    }

    private float getWrap() {
        return this.getEffect().getWrap() ? 1.0f : 0.0f;
    }

    @Override
    protected Object getSamplerData(int n) {
        return this.getEffect().getMapData();
    }

    @Override
    public int getTextureCoordinates(int n, float[] fArray, float f, float f2, float f3, float f4, Rectangle rectangle, BaseTransform baseTransform) {
        fArray[1] = 0.0f;
        fArray[0] = 0.0f;
        fArray[3] = 1.0f;
        fArray[2] = 1.0f;
        return 4;
    }

    @Override
    public ImageData filter(Effect effect, RenderState renderState, BaseTransform baseTransform, Rectangle rectangle, ImageData ... imageDataArray) {
        this.setEffect(effect);
        Rectangle rectangle2 = this.getResultBounds(baseTransform, rectangle, imageDataArray);
        this.setDestBounds(rectangle2);
        HeapImage heapImage = (HeapImage)imageDataArray[0].getUntransformedImage();
        int n = 0;
        int n2 = 0;
        int n3 = heapImage.getPhysicalWidth();
        int n4 = heapImage.getPhysicalHeight();
        int n5 = heapImage.getScanlineStride();
        int[] nArray = heapImage.getPixelArray();
        Rectangle rectangle3 = new Rectangle(n, n2, n3, n4);
        Rectangle rectangle4 = imageDataArray[0].getUntransformedBounds();
        BaseTransform baseTransform2 = imageDataArray[0].getTransform();
        this.setInputBounds(0, rectangle4);
        this.setInputNativeBounds(0, rectangle3);
        float[] fArray = new float[4];
        FloatMap floatMap = (FloatMap)this.getSamplerData(1);
        boolean bl = false;
        boolean bl2 = false;
        int n6 = floatMap.getWidth();
        int n7 = floatMap.getHeight();
        int n8 = floatMap.getWidth();
        float[] fArray2 = floatMap.getData();
        float[] fArray3 = new float[4];
        float[] fArray4 = new float[4];
        this.getTextureCoordinates(0, fArray4, rectangle4.x, rectangle4.y, n3, n4, rectangle2, baseTransform2);
        float[] fArray5 = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
        int n9 = rectangle2.width;
        int n10 = rectangle2.height;
        HeapImage heapImage2 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n9, n10));
        this.setDestNativeBounds(heapImage2.getPhysicalWidth(), heapImage2.getPhysicalHeight());
        int n11 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        float[] fArray6 = this.getImagetx();
        float f = fArray6[0];
        float f2 = fArray6[1];
        float f3 = fArray6[2];
        float f4 = fArray6[3];
        float f5 = this.getWrap();
        float[] fArray7 = this.getSampletx();
        float f6 = fArray7[0];
        float f7 = fArray7[1];
        float f8 = fArray7[2];
        float f9 = fArray7[3];
        float f10 = (fArray4[2] - fArray4[0]) / (float)n9;
        float f11 = (fArray4[3] - fArray4[1]) / (float)n10;
        float f12 = (fArray5[2] - fArray5[0]) / (float)n9;
        float f13 = (fArray5[3] - fArray5[1]) / (float)n10;
        float f14 = fArray4[1] + f11 * 0.5f;
        float f15 = fArray5[1] + f13 * 0.5f;
        for (int i = 0; i < 0 + n10; ++i) {
            float f16 = i;
            int n12 = i * n11;
            float f17 = fArray4[0] + f10 * 0.5f;
            float f18 = fArray5[0] + f12 * 0.5f;
            for (int j = 0; j < 0 + n9; ++j) {
                float f19 = j;
                float f20 = f18;
                float f21 = f15;
                this.fsample(fArray2, f20, f21, n6, n7, n8, fArray3);
                float f22 = fArray3[0];
                float f23 = fArray3[1];
                float f24 = fArray3[2];
                float f25 = fArray3[3];
                f20 = f22;
                f21 = f23;
                float f26 = f24;
                float f27 = f25;
                float f28 = f17 + f8 * (f20 + f6);
                float f29 = f14 + f9 * (f21 + f7);
                float f30 = f28;
                float f31 = f29;
                float f32 = (float)Math.floor(f30);
                float f33 = (float)Math.floor(f31);
                f28 -= f5 * f32;
                f29 -= f5 * f33;
                f28 = f + f28 * f3;
                f29 = f2 + f29 * f4;
                f30 = f28;
                f31 = f29;
                this.lsample(nArray, f30, f31, n3, n4, n5, fArray);
                f22 = fArray[0];
                f23 = fArray[1];
                f24 = fArray[2];
                f25 = fArray[3];
                float f34 = f22;
                float f35 = f23;
                float f36 = f24;
                float f37 = f25;
                if (f37 < 0.0f) {
                    f37 = 0.0f;
                } else if (f37 > 1.0f) {
                    f37 = 1.0f;
                }
                if (f34 < 0.0f) {
                    f34 = 0.0f;
                } else if (f34 > f37) {
                    f34 = f37;
                }
                if (f35 < 0.0f) {
                    f35 = 0.0f;
                } else if (f35 > f37) {
                    f35 = f37;
                }
                if (f36 < 0.0f) {
                    f36 = 0.0f;
                } else if (f36 > f37) {
                    f36 = f37;
                }
                nArray2[n12 + j] = (int)(f34 * 255.0f) << 16 | (int)(f35 * 255.0f) << 8 | (int)(f36 * 255.0f) << 0 | (int)(f37 * 255.0f) << 24;
                f17 += f10;
                f18 += f12;
            }
            f14 += f11;
            f15 += f13;
        }
        return new ImageData(this.getFilterContext(), heapImage2, rectangle2);
    }
}

