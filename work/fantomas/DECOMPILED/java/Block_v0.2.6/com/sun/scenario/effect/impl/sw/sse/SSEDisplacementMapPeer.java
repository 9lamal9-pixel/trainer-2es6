/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.sse;

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
import com.sun.scenario.effect.impl.sw.sse.SSEEffectPeer;

public class SSEDisplacementMapPeer
extends SSEEffectPeer {
    public SSEDisplacementMapPeer(FilterContext filterContext, Renderer renderer, String string) {
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
        FloatMap floatMap = (FloatMap)this.getSamplerData(1);
        boolean bl = false;
        boolean bl2 = false;
        int n = floatMap.getWidth();
        int n2 = floatMap.getHeight();
        int n3 = floatMap.getWidth();
        float[] fArray = floatMap.getData();
        HeapImage heapImage = (HeapImage)imageDataArray[0].getUntransformedImage();
        int n4 = 0;
        int n5 = 0;
        int n6 = heapImage.getPhysicalWidth();
        int n7 = heapImage.getPhysicalHeight();
        int n8 = heapImage.getScanlineStride();
        int[] nArray = heapImage.getPixelArray();
        Rectangle rectangle3 = new Rectangle(n4, n5, n6, n7);
        Rectangle rectangle4 = imageDataArray[0].getUntransformedBounds();
        BaseTransform baseTransform2 = imageDataArray[0].getTransform();
        this.setInputBounds(0, rectangle4);
        this.setInputNativeBounds(0, rectangle3);
        float[] fArray2 = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
        float[] fArray3 = new float[4];
        this.getTextureCoordinates(0, fArray3, rectangle4.x, rectangle4.y, n6, n7, rectangle2, baseTransform2);
        int n9 = rectangle2.width;
        int n10 = rectangle2.height;
        HeapImage heapImage2 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n9, n10));
        this.setDestNativeBounds(heapImage2.getPhysicalWidth(), heapImage2.getPhysicalHeight());
        int n11 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        float[] fArray4 = this.getImagetx();
        float[] fArray5 = this.getSampletx();
        float f = this.getWrap();
        SSEDisplacementMapPeer.filter(nArray2, 0, 0, n9, n10, n11, fArray4[0], fArray4[1], fArray4[2], fArray4[3], fArray, fArray2[0], fArray2[1], fArray2[2], fArray2[3], n, n2, n3, nArray, fArray3[0], fArray3[1], fArray3[2], fArray3[3], n6, n7, n8, fArray5[0], fArray5[1], fArray5[2], fArray5[3], f);
        return new ImageData(this.getFilterContext(), heapImage2, rectangle2);
    }

    private static native void filter(int[] var0, int var1, int var2, int var3, int var4, int var5, float var6, float var7, float var8, float var9, float[] var10, float var11, float var12, float var13, float var14, int var15, int var16, int var17, int[] var18, float var19, float var20, float var21, float var22, int var23, int var24, int var25, float var26, float var27, float var28, float var29, float var30);
}

