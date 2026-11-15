/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.sg.prism;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.sg.prism.NGShape;

public class NGPolyline
extends NGShape {
    private Path2D path = new Path2D();

    public void updatePolyline(float[] fArray) {
        this.path.reset();
        if (fArray == null || fArray.length == 0 || fArray.length % 2 != 0) {
            return;
        }
        this.path.moveTo(fArray[0], fArray[1]);
        for (int i = 1; i < fArray.length / 2; ++i) {
            float f = fArray[i * 2 + 0];
            float f2 = fArray[i * 2 + 1];
            this.path.lineTo(f, f2);
        }
        this.geometryChanged();
    }

    @Override
    public Shape getShape() {
        return this.path;
    }
}

