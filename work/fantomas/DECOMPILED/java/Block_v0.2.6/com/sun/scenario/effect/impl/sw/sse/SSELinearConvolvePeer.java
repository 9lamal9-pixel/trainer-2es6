/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.sse;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.LinearConvolveRenderState;
import com.sun.scenario.effect.impl.sw.sse.SSEEffectPeer;
import java.nio.FloatBuffer;

public class SSELinearConvolvePeer
extends SSEEffectPeer<LinearConvolveRenderState> {
    public SSELinearConvolvePeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    public ImageData filter(Effect effect, LinearConvolveRenderState linearConvolveRenderState, BaseTransform baseTransform, Rectangle rectangle, ImageData ... imageDataArray) {
        this.setRenderState(linearConvolveRenderState);
        Rectangle rectangle2 = imageDataArray[0].getTransformedBounds(null);
        Rectangle rectangle3 = linearConvolveRenderState.getPassResultBounds(rectangle2, null);
        Rectangle rectangle4 = linearConvolveRenderState.getPassResultBounds(rectangle2, rectangle);
        this.setDestBounds(rectangle4);
        int n = rectangle4.width;
        int n2 = rectangle4.height;
        HeapImage heapImage = (HeapImage)imageDataArray[0].getUntransformedImage();
        int n3 = heapImage.getPhysicalWidth();
        int n4 = heapImage.getPhysicalHeight();
        int n5 = heapImage.getScanlineStride();
        int[] nArray = heapImage.getPixelArray();
        Rectangle rectangle5 = imageDataArray[0].getUntransformedBounds();
        BaseTransform baseTransform2 = imageDataArray[0].getTransform();
        Rectangle rectangle6 = new Rectangle(0, 0, n3, n4);
        this.setInputBounds(0, rectangle5);
        this.setInputTransform(0, baseTransform2);
        this.setInputNativeBounds(0, rectangle6);
        HeapImage heapImage2 = (HeapImage)((Object)this.getRenderer().getCompatibleImage(n, n2));
        this.setDestNativeBounds(heapImage2.getPhysicalWidth(), heapImage2.getPhysicalHeight());
        int n6 = heapImage2.getScanlineStride();
        int[] nArray2 = heapImage2.getPixelArray();
        int n7 = linearConvolveRenderState.getPassKernelSize();
        FloatBuffer floatBuffer = linearConvolveRenderState.getPassWeights();
        LinearConvolveRenderState.PassType passType = linearConvolveRenderState.getPassType();
        if (!baseTransform2.isIdentity() || !rectangle4.contains(rectangle3.x, rectangle3.y)) {
            passType = LinearConvolveRenderState.PassType.GENERAL_VECTOR;
        }
        if (passType == LinearConvolveRenderState.PassType.HORIZONTAL_CENTERED) {
            float[] fArray = new float[n7 * 2];
            floatBuffer.get(fArray, 0, n7);
            floatBuffer.rewind();
            floatBuffer.get(fArray, n7, n7);
            this.filterHV(nArray2, n, n2, 1, n6, nArray, n3, n4, 1, n5, fArray);
        } else if (passType == LinearConvolveRenderState.PassType.VERTICAL_CENTERED) {
            float[] fArray = new float[n7 * 2];
            floatBuffer.get(fArray, 0, n7);
            floatBuffer.rewind();
            floatBuffer.get(fArray, n7, n7);
            this.filterHV(nArray2, n2, n, n6, 1, nArray, n4, n3, n5, 1, fArray);
        } else {
            float f;
            float f2;
            float f3;
            float f4;
            float[] fArray = new float[n7];
            floatBuffer.get(fArray, 0, n7);
            float[] fArray2 = new float[8];
            int n8 = this.getTextureCoordinates(0, fArray2, rectangle5.x, rectangle5.y, rectangle6.width, rectangle6.height, rectangle4, baseTransform2);
            float f5 = fArray2[0] * (float)n3;
            float f6 = fArray2[1] * (float)n4;
            if (n8 < 8) {
                f4 = (fArray2[2] - fArray2[0]) * (float)n3 / (float)rectangle4.width;
                f3 = 0.0f;
                f2 = 0.0f;
                f = (fArray2[3] - fArray2[1]) * (float)n4 / (float)rectangle4.height;
            } else {
                f4 = (fArray2[4] - fArray2[0]) * (float)n3 / (float)rectangle4.width;
                f3 = (fArray2[5] - fArray2[1]) * (float)n4 / (float)rectangle4.height;
                f2 = (fArray2[6] - fArray2[0]) * (float)n3 / (float)rectangle4.width;
                f = (fArray2[7] - fArray2[1]) * (float)n4 / (float)rectangle4.height;
            }
            float[] fArray3 = linearConvolveRenderState.getPassVector();
            float f7 = fArray3[0] * (float)n3;
            float f8 = fArray3[1] * (float)n4;
            float f9 = fArray3[2] * (float)n3;
            float f10 = fArray3[3] * (float)n4;
            this.filterVector(nArray2, n, n2, n6, nArray, n3, n4, n5, fArray, n7, f5, f6, f9, f10, f7, f8, f4, f3, f2, f);
        }
        return new ImageData(this.getFilterContext(), heapImage2, rectangle4);
    }

    native void filterVector(int[] var1, int var2, int var3, int var4, int[] var5, int var6, int var7, int var8, float[] var9, int var10, float var11, float var12, float var13, float var14, float var15, float var16, float var17, float var18, float var19, float var20);

    native void filterHV(int[] var1, int var2, int var3, int var4, int var5, int[] var6, int var7, int var8, int var9, int var10, float[] var11);
}

