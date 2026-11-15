/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;

public final class CollinearSimplifier
implements PathConsumer2D {
    static final float EPS = 1.0E-4f;
    PathConsumer2D delegate;
    SimplifierState state;
    float px1;
    float py1;
    float px2;
    float py2;
    float pslope;

    CollinearSimplifier() {
    }

    public CollinearSimplifier init(PathConsumer2D pathConsumer2D) {
        this.delegate = pathConsumer2D;
        this.state = SimplifierState.Empty;
        return this;
    }

    @Override
    public void pathDone() {
        this.emitStashedLine();
        this.state = SimplifierState.Empty;
        this.delegate.pathDone();
    }

    @Override
    public void closePath() {
        this.emitStashedLine();
        this.state = SimplifierState.Empty;
        this.delegate.closePath();
    }

    @Override
    public void quadTo(float f, float f2, float f3, float f4) {
        this.emitStashedLine();
        this.delegate.quadTo(f, f2, f3, f4);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = f3;
        this.py1 = f4;
    }

    @Override
    public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        this.emitStashedLine();
        this.delegate.curveTo(f, f2, f3, f4, f5, f6);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = f5;
        this.py1 = f6;
    }

    @Override
    public void moveTo(float f, float f2) {
        this.emitStashedLine();
        this.delegate.moveTo(f, f2);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = f;
        this.py1 = f2;
    }

    @Override
    public void lineTo(float f, float f2) {
        switch (this.state) {
            case Empty: {
                this.delegate.lineTo(f, f2);
                this.state = SimplifierState.PreviousPoint;
                this.px1 = f;
                this.py1 = f2;
                return;
            }
            case PreviousPoint: {
                this.state = SimplifierState.PreviousLine;
                this.px2 = f;
                this.py2 = f2;
                this.pslope = CollinearSimplifier.getSlope(this.px1, this.py1, f, f2);
                return;
            }
            case PreviousLine: {
                float f3 = CollinearSimplifier.getSlope(this.px2, this.py2, f, f2);
                if (f3 == this.pslope || Math.abs(this.pslope - f3) < 1.0E-4f) {
                    this.px2 = f;
                    this.py2 = f2;
                    return;
                }
                this.delegate.lineTo(this.px2, this.py2);
                this.px1 = this.px2;
                this.py1 = this.py2;
                this.px2 = f;
                this.py2 = f2;
                this.pslope = f3;
                return;
            }
        }
    }

    private void emitStashedLine() {
        if (this.state == SimplifierState.PreviousLine) {
            this.delegate.lineTo(this.px2, this.py2);
        }
    }

    private static float getSlope(float f, float f2, float f3, float f4) {
        float f5 = f4 - f2;
        if (f5 == 0.0f) {
            return f3 > f ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }
        return (f3 - f) / f5;
    }

    static enum SimplifierState {
        Empty,
        PreviousPoint,
        PreviousLine;

    }
}

