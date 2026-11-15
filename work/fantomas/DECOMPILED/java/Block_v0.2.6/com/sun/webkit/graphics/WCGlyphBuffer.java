/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

public class WCGlyphBuffer {
    public int[] glyphs;
    public float[] advances;
    public float initialAdvance;

    public WCGlyphBuffer(int[] nArray, float[] fArray, float f) {
        this.glyphs = nArray;
        this.advances = fArray;
        this.initialAdvance = f;
    }
}

