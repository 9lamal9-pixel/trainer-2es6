/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.sse;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.PerspectiveTransform;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.AccessHelper;
import com.sun.scenario.effect.impl.state.PerspectiveTransformState;
import com.sun.scenario.effect.impl.state.RenderState;
import com.sun.scenario.effect.impl.sw.sse.SSEEffectPeer;

public class SSEPerspectiveTransformPeer
extends SSEEffectPeer {
    public SSEPerspectiveTransformPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    protected final PerspectiveTransform getEffect() {
        return (PerspectiveTransform)super.getEffect();
    }

    private float[][] getITX() {
        PerspectiveTransformState perspectiveTransformState = (PerspectiveTransformState)AccessHelper.getState(this.getEffect());
        return perspectiveTransformState.getITX();
    }

    private float[] getTx0() {
        Rectangle rectangle = this.getInputBounds(0);
        Rectangle rectangle2 = this.getInputNativeBounds(0);
        float f = (float)rectangle.width / (float)rectangle2.width;
        float[] fArray = this.getITX()[0];
        return new float[]{fArray[0] * f, fArray[1] * f, fArray[2] * f};
    }

    private float[] getTx1() {
        Rectangle rectangle = this.getInputBounds(0);
        Rectangle rectangle2 = this.getInputNativeBounds(0);
        float f = (float)rectangle.height / (float)rectangle2.height;
        float[] fArray = this.getITX()[1];
        return new float[]{fArray[0] * f, fArray[1] * f, fArray[2] * f};
    }

    private float[] getTx2() {
        return this.getITX()[2];
    }

    @Override
    public int getTextureCoordinates(int n, float[] fArray, float f, float f2, float f3, float f4, Rectangle rectangle, BaseTransform baseTransform) {
        fArray[0] = rectangle.x;
        fArray[1] = rectangle.y;
        fArray[2] = rectangle.x + rectangle.width;
        fArray[3] = rectangle.y + rectangle.height;
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
        this.getTextureCoordinates(0, fArray, rectangle4.x, rectangle4.y, n3, n4, rectangle2, baseTransform2);
        int n6 = rectangle2.width;
        int n7 = rectangle2.height;
        HeapImage heapImage2 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n6, n7));
        this.setDestNativeBounds(heapImage2.getPhysicalWidth(), heapImage2.getPhysicalHeight());
        int n8 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        float[] fArray2 = this.getTx0();
        float[] fArray3 = this.getTx1();
        float[] fArray4 = this.getTx2();
        SSEPerspectiveTransformPeer.filter(nArray2, 0, 0, n6, n7, n8, nArray, fArray[0], fArray[1], fArray[2], fArray[3], n3, n4, n5, fArray2[0], fArray2[1], fArray2[2], fArray3[0], fArray3[1], fArray3[2], fArray4[0], fArray4[1], fArray4[2]);
        return new ImageData(this.getFilterContext(), heapImage2, rectangle2);
    }

    private static native void filter(int[] var0, int var1, int var2, int var3, int var4, int var5, int[] var6, float var7, float var8, float var9, float var10, int var11, int var12, int var13, float var14, float var15, float var16, float var17, float var18, float var19, float var20, float var21, float var22);
}

