/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.prism.ps;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.prism.ps.Shader;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.PerspectiveTransform;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.prism.ps.PPSOneSamplerPeer;
import com.sun.scenario.effect.impl.state.AccessHelper;
import com.sun.scenario.effect.impl.state.PerspectiveTransformState;
import java.util.HashMap;

public class PPSPerspectiveTransformPeer
extends PPSOneSamplerPeer {
    public PPSPerspectiveTransformPeer(FilterContext filterContext, Renderer renderer, String string) {
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
    protected boolean isSamplerLinear(int n) {
        switch (n) {
            case 0: {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Shader createShader() {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        hashMap.put("baseImg", 0);
        HashMap<String, Integer> hashMap2 = new HashMap<String, Integer>();
        hashMap2.put("tx1", 1);
        hashMap2.put("tx0", 0);
        hashMap2.put("tx2", 2);
        return this.getRenderer().createShader(this.getShaderName(), hashMap, hashMap2, false);
    }

    @Override
    protected void updateShader(Shader shader) {
        float[] fArray = this.getTx1();
        shader.setConstant("tx1", fArray[0], fArray[1], fArray[2]);
        float[] fArray2 = this.getTx0();
        shader.setConstant("tx0", fArray2[0], fArray2[1], fArray2[2]);
        float[] fArray3 = this.getTx2();
        shader.setConstant("tx2", fArray3[0], fArray3[1], fArray3[2]);
    }
}

