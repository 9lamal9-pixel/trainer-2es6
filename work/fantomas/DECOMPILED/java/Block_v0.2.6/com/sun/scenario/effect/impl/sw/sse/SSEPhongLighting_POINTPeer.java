/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.sw.sse;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.ImageData;
import com.sun.scenario.effect.PhongLighting;
import com.sun.scenario.effect.impl.BufferUtil;
import com.sun.scenario.effect.impl.HeapImage;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.state.RenderState;
import com.sun.scenario.effect.impl.sw.sse.SSEEffectPeer;
import com.sun.scenario.effect.light.PointLight;
import com.sun.scenario.effect.light.SpotLight;
import java.nio.FloatBuffer;

public class SSEPhongLighting_POINTPeer
extends SSEEffectPeer {
    private FloatBuffer kvals;

    public SSEPhongLighting_POINTPeer(FilterContext filterContext, Renderer renderer, String string) {
        super(filterContext, renderer, string);
    }

    @Override
    protected final PhongLighting getEffect() {
        return (PhongLighting)super.getEffect();
    }

    private float getSurfaceScale() {
        return this.getEffect().getSurfaceScale();
    }

    private float getDiffuseConstant() {
        return this.getEffect().getDiffuseConstant();
    }

    private float getSpecularConstant() {
        return this.getEffect().getSpecularConstant();
    }

    private float getSpecularExponent() {
        return this.getEffect().getSpecularExponent();
    }

    private float[] getNormalizedLightPosition() {
        return this.getEffect().getLight().getNormalizedLightPosition();
    }

    private float[] getLightPosition() {
        PointLight pointLight = (PointLight)this.getEffect().getLight();
        return new float[]{pointLight.getX(), pointLight.getY(), pointLight.getZ()};
    }

    private float[] getLightColor() {
        return this.getEffect().getLight().getColor().getPremultipliedRGBComponents();
    }

    private float getLightSpecularExponent() {
        return ((SpotLight)this.getEffect().getLight()).getSpecularExponent();
    }

    private float[] getNormalizedLightDirection() {
        return ((SpotLight)this.getEffect().getLight()).getNormalizedLightDirection();
    }

    private FloatBuffer getKvals() {
        Rectangle rectangle = this.getInputNativeBounds(0);
        float f = 1.0f / (float)rectangle.width;
        float f2 = 1.0f / (float)rectangle.height;
        float[] fArray = new float[]{-1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f};
        float[] fArray2 = new float[]{-1.0f, -2.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f};
        if (this.kvals == null) {
            this.kvals = BufferUtil.newFloatBuffer(32);
        }
        this.kvals.clear();
        int n = 0;
        float f3 = -this.getSurfaceScale() * 0.25f;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i != 0 || j != 0) {
                    this.kvals.put((float)j * f);
                    this.kvals.put((float)i * f2);
                    this.kvals.put(fArray[n] * f3);
                    this.kvals.put(fArray2[n] * f3);
                }
                ++n;
            }
        }
        this.kvals.rewind();
        return this.kvals;
    }

    private int getKvalsArrayLength() {
        return 8;
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
        float f = this.getDiffuseConstant();
        FloatBuffer floatBuffer = this.getKvals();
        float[] fArray3 = new float[floatBuffer.capacity()];
        floatBuffer.get(fArray3);
        float[] fArray4 = this.getLightColor();
        float[] fArray5 = this.getLightPosition();
        float f2 = this.getSpecularConstant();
        float f3 = this.getSpecularExponent();
        float f4 = this.getSurfaceScale();
        SSEPhongLighting_POINTPeer.filter(nArray3, 0, 0, n11, n12, n13, nArray, fArray[0], fArray[1], fArray[2], fArray[3], n3, n4, n5, f, fArray3, fArray4[0], fArray4[1], fArray4[2], fArray5[0], fArray5[1], fArray5[2], nArray2, fArray2[0], fArray2[1], fArray2[2], fArray2[3], n8, n9, n10, f2, f3, f4);
        imageDataArray[0].releaseTransformedImage(heapImage);
        imageDataArray[1].releaseTransformedImage(heapImage2);
        return new ImageData(this.getFilterContext(), heapImage3, rectangle2);
    }

    private static native void filter(int[] var0, int var1, int var2, int var3, int var4, int var5, int[] var6, float var7, float var8, float var9, float var10, int var11, int var12, int var13, float var14, float[] var15, float var16, float var17, float var18, float var19, float var20, float var21, int[] var22, float var23, float var24, float var25, float var26, int var27, int var28, int var29, float var30, float var31, float var32);
}

