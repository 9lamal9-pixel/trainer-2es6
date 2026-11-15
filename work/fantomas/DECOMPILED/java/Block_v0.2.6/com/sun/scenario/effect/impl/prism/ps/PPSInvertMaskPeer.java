/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.impl.prism.ps;

import com.sun.prism.ps.Shader;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.InvertMask;
import com.sun.scenario.effect.impl.Renderer;
import com.sun.scenario.effect.impl.prism.ps.PPSOneSamplerPeer;
import java.util.HashMap;

public class PPSInvertMaskPeer
extends PPSOneSamplerPeer {
    public PPSInvertMaskPeer(FilterContext filterContext, Renderer renderer, String string) {
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
    protected boolean isSamplerLinear(int n) {
        switch (n) {
            default: 
        }
        return false;
    }

    @Override
    protected Shader createShader() {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        hashMap.put("baseImg", 0);
        HashMap<String, Integer> hashMap2 = new HashMap<String, Integer>();
        hashMap2.put("offset", 0);
        return this.getRenderer().createShader(this.getShaderName(), hashMap, hashMap2, false);
    }

    @Override
    protected void updateShader(Shader shader) {
        float[] fArray = this.getOffset();
        shader.setConstant("offset", fArray[0], fArray[1]);
    }
}

