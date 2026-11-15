/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;
import com.sun.marlin.MarlinProperties;

public final class PathSimplifier
implements PathConsumer2D {
    private static final float PIX_THRESHOLD = MarlinProperties.getPathSimplifierPixelTolerance();
    private static final float SQUARE_TOLERANCE = PIX_THRESHOLD * PIX_THRESHOLD;
    private PathConsumer2D delegate;
    private float cx;
    private float cy;
    private boolean skipped;
    private float sx;
    private float sy;

    PathSimplifier() {
    }

    public PathSimplifier init(PathConsumer2D pathConsumer2D) {
        this.delegate = pathConsumer2D;
        this.skipped = false;
        return this;
    }

    private void finishPath() {
        if (this.skipped) {
            this._lineTo(this.sx, this.sy);
        }
    }

    @Override
    public void pathDone() {
        this.finishPath();
        this.delegate.pathDone();
    }

    @Override
    public void closePath() {
        this.finishPath();
        this.delegate.closePath();
    }

    @Override
    public void moveTo(float f, float f2) {
        this.finishPath();
        this.delegate.moveTo(f, f2);
        this.cx = f;
        this.cy = f2;
    }

    @Override
    public void lineTo(float f, float f2) {
        float f3 = f - this.cx;
        float f4 = f2 - this.cy;
        if (f3 * f3 + f4 * f4 <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = f;
            this.sy = f2;
            return;
        }
        this._lineTo(f, f2);
    }

    private void _lineTo(float f, float f2) {
        this.delegate.lineTo(f, f2);
        this.cx = f;
        this.cy = f2;
        this.skipped = false;
    }

    @Override
    public void quadTo(float f, float f2, float f3, float f4) {
        float f5 = f3 - this.cx;
        float f6 = f4 - this.cy;
        if (f5 * f5 + f6 * f6 <= SQUARE_TOLERANCE && (f5 = f - this.cx) * f5 + (f6 = f2 - this.cy) * f6 <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = f3;
            this.sy = f4;
            return;
        }
        this.delegate.quadTo(f, f2, f3, f4);
        this.cx = f3;
        this.cy = f4;
        this.skipped = false;
    }

    @Override
    public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = f5 - this.cx;
        float f8 = f6 - this.cy;
        if (f7 * f7 + f8 * f8 <= SQUARE_TOLERANCE && (f7 = f - this.cx) * f7 + (f8 = f2 - this.cy) * f8 <= SQUARE_TOLERANCE && (f7 = f3 - this.cx) * f7 + (f8 = f4 - this.cy) * f8 <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = f5;
            this.sy = f6;
            return;
        }
        this.delegate.curveTo(f, f2, f3, f4, f5, f6);
        this.cx = f5;
        this.cy = f6;
        this.skipped = false;
    }
}

