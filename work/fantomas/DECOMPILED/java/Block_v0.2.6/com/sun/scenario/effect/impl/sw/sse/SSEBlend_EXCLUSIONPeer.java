/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.sse;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.Blend;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.RenderState;
import com.sun.scenario.effect.impl.sw.sse.SSEEffectPeer;

public class SSEBlend_EXCLUSIONPeer
extends SSEEffectPeer {
    public SSEBlend_EXCLUSIONPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    protected final Blend getEffect() {
        return (Blend)super.getEffect();
    }

    private float getOpacity() {
        return this.getEffect().getOpacity();
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
        HeapImage heapImage2 = (HeapImage)imageDataArray[1].getTransformedImage(rectangle2);
        int n6 = 0;
        int n7 = 0;
        int n8 = heapImage2.getPhysicalWidth();
        int n9 = heapImage2.getPhysicalHeight();
        int n10 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        Rectangle rectangle5 = new Rectangle(n6, n7, n8, n9);
        Rectangle rectangle6 = imageDataArray[1].getTransformedBounds(rectangle2);
        BaseTransform baseTransform3 = BaseTransform.IDENTITY_TRANSFORM;
        this.setInputBounds(1, rectangle6);
        this.setInputNativeBounds(1, rectangle5);
        float[] fArray = new float[4];
        this.getTextureCoordinates(0, fArray, rectangle4.x, rectangle4.y, n3, n4, rectangle2, baseTransform2);
        float[] fArray2 = new float[4];
        this.getTextureCoordinates(1, fArray2, rectangle6.x, rectangle6.y, n8, n9, rectangle2, baseTransform3);
        int n11 = rectangle2.width;
        int n12 = rectangle2.height;
        HeapImage heapImage3 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n11, n12));
        this.setDestNativeBounds(heapImage3.getPhysicalWidth(), heapImage3.getPhysicalHeight());
        int n13 = heapImage3.getScanlineStride();
        int[] nArray3 = heapImage3.getPixelArray();
        float f = this.getOpacity();
        SSEBlend_EXCLUSIONPeer.filter(nArray3, 0, 0, n11, n12, n13, nArray, fArray[0], fArray[1], fArray[2], fArray[3], n3, n4, n5, f, nArray2, fArray2[0], fArray2[1], fArray2[2], fArray2[3], n8, n9, n10);
        imageDataArray[0].releaseTransformedImage(heapImage);
        imageDataArray[1].releaseTransformedImage(heapImage2);
        return new ImageData(this.getFilterContext(), heapImage3, rectangle2);
    }

    private static native void filter(int[] var0, int var1, int var2, int var3, int var4, int var5, int[] var6, float var7, float var8, float var9, float var10, int var11, int var12, int var13, float var14, int[] var15, float var16, float var17, float var18, float var19, int var20, int var21, int var22);
}

