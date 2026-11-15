/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;
import com.sun.marlin.MarlinAlphaConsumer;

public interface MarlinRenderer
extends PathConsumer2D {
    public MarlinRenderer init(int var1, int var2, int var3, int var4, int var5);

    public void dispose();

    public int getOutpixMinX();

    public int getOutpixMaxX();

    public int getOutpixMinY();

    public int getOutpixMaxY();

    public void produceAlphas(MarlinAlphaConsumer var1);

    public float getOffsetX();

    public float getOffsetY();
}

