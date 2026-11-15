/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.j2d.paint;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.lang.ref.SoftReference;

public abstract class MultipleGradientPaint
implements Paint {
    final int transparency;
    final float[] fractions;
    final Color[] colors;
    final AffineTransform gradientTransform;
    final CycleMethod cycleMethod;
    final ColorSpaceType colorSpace;
    ColorModel model;
    float[] normalizedIntervals;
    boolean isSimpleLookup;
    SoftReference<int[][]> gradients;
    SoftReference<int[]> gradient;
    int fastGradientArraySize;

    MultipleGradientPaint(float[] fArray, Color[] colorArray, CycleMethod cycleMethod, ColorSpaceType colorSpaceType, AffineTransform affineTransform) {
        if (fArray == null) {
            throw new NullPointerException("Fractions array cannot be null");
        }
        if (colorArray == null) {
            throw new NullPointerException("Colors array cannot be null");
        }
        if (cycleMethod == null) {
            throw new NullPointerException("Cycle method cannot be null");
        }
        if (colorSpaceType == null) {
            throw new NullPointerException("Color space cannot be null");
        }
        if (affineTransform == null) {
            throw new NullPointerException("Gradient transform cannot be null");
        }
        if (fArray.length != colorArray.length) {
            throw new IllegalArgumentException("Colors and fractions must have equal size");
        }
        if (colorArray.length < 2) {
            throw new IllegalArgumentException("User must specify at least 2 colors");
        }
        float f = -1.0f;
        for (float f2 : fArray) {
            if (f2 < 0.0f || f2 > 1.0f) {
                throw new IllegalArgumentException("Fraction values must be in the range 0 to 1: " + f2);
            }
            if (f2 <= f) {
                throw new IllegalArgumentException("Keyframe fractions must be increasing: " + f2);
            }
            f = f2;
        }
        boolean bl = false;
        int n = 0;
        int n2 = fArray.length;
        int n3 = 0;
        if (fArray[0] != 0.0f) {
            bl = true;
            ++n2;
            ++n3;
        }
        if (fArray[fArray.length - 1] != 1.0f) {
            n = 1;
            ++n2;
        }
        this.fractions = new float[n2];
        System.arraycopy(fArray, 0, this.fractions, n3, fArray.length);
        this.colors = new Color[n2];
        System.arraycopy(colorArray, 0, this.colors, n3, colorArray.length);
        if (bl) {
            this.fractions[0] = 0.0f;
            this.colors[0] = colorArray[0];
        }
        if (n != 0) {
            this.fractions[n2 - 1] = 1.0f;
            this.colors[n2 - 1] = colorArray[colorArray.length - 1];
        }
        this.colorSpace = colorSpaceType;
        this.cycleMethod = cycleMethod;
        this.gradientTransform = new AffineTransform(affineTransform);
        boolean bl2 = true;
        for (int i = 0; i < colorArray.length; ++i) {
            bl2 = bl2 && colorArray[i].getAlpha() == 255;
        }
        this.transparency = bl2 ? 1 : 3;
    }

    public final float[] getFractions() {
        float[] fArray = new float[this.fractions.length];
        System.arraycopy(this.fractions, 0, fArray, 0, this.fractions.length);
        return fArray;
    }

    public final Color[] getColors() {
        Color[] colorArray = new Color[this.fractions.length];
        System.arraycopy(this.fractions, 0, colorArray, 0, this.fractions.length);
        return colorArray;
    }

    public final CycleMethod getCycleMethod() {
        return this.cycleMethod;
    }

    public final ColorSpaceType getColorSpace() {
        return this.colorSpace;
    }

    public final AffineTransform getTransform() {
        return new AffineTransform(this.gradientTransform);
    }

    @Override
    public final int getTransparency() {
        return this.transparency;
    }

    public static enum ColorSpaceType {
        SRGB,
        LINEAR_RGB;

    }

    public static enum CycleMethod {
        NO_CYCLE,
        REFLECT,
        REPEAT;

    }
}

