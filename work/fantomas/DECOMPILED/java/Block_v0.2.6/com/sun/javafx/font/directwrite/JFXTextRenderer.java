/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.font.directwrite;

import com.sun.javafx.font.directwrite.IDWriteFontFace;
import com.sun.javafx.font.directwrite.IUnknown;
import com.sun.javafx.font.directwrite.OS;

class JFXTextRenderer
extends IUnknown {
    JFXTextRenderer(long l) {
        super(l);
    }

    boolean Next() {
        return OS.JFXTextRendererNext(this.ptr);
    }

    int GetStart() {
        return OS.JFXTextRendererGetStart(this.ptr);
    }

    int GetLength() {
        return OS.JFXTextRendererGetLength(this.ptr);
    }

    int GetGlyphCount() {
        return OS.JFXTextRendererGetGlyphCount(this.ptr);
    }

    int GetTotalGlyphCount() {
        return OS.JFXTextRendererGetTotalGlyphCount(this.ptr);
    }

    IDWriteFontFace GetFontFace() {
        long l = OS.JFXTextRendererGetFontFace(this.ptr);
        return l != 0L ? new IDWriteFontFace(l) : null;
    }

    int GetGlyphIndices(int[] nArray, int n, int n2) {
        return OS.JFXTextRendererGetGlyphIndices(this.ptr, nArray, n, n2);
    }

    int GetGlyphAdvances(float[] fArray, int n) {
        return OS.JFXTextRendererGetGlyphAdvances(this.ptr, fArray, n);
    }

    int GetGlyphOffsets(float[] fArray, int n) {
        return OS.JFXTextRendererGetGlyphOffsets(this.ptr, fArray, n);
    }

    int GetClusterMap(short[] sArray, int n, int n2) {
        return OS.JFXTextRendererGetClusterMap(this.ptr, sArray, n, n2);
    }
}

