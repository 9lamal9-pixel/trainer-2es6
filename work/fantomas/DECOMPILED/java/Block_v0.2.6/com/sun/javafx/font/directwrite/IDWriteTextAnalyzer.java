/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.font.directwrite;

import com.sun.javafx.font.directwrite.DWRITE_SCRIPT_ANALYSIS;
import com.sun.javafx.font.directwrite.IDWriteFontFace;
import com.sun.javafx.font.directwrite.IUnknown;
import com.sun.javafx.font.directwrite.JFXTextAnalysisSink;
import com.sun.javafx.font.directwrite.OS;

class IDWriteTextAnalyzer
extends IUnknown {
    IDWriteTextAnalyzer(long l) {
        super(l);
    }

    int AnalyzeScript(JFXTextAnalysisSink jFXTextAnalysisSink, int n, int n2, JFXTextAnalysisSink jFXTextAnalysisSink2) {
        return OS.AnalyzeScript(this.ptr, jFXTextAnalysisSink.ptr, n, n2, jFXTextAnalysisSink2.ptr);
    }

    int GetGlyphs(char[] cArray, int n, int n2, IDWriteFontFace iDWriteFontFace, boolean bl, boolean bl2, DWRITE_SCRIPT_ANALYSIS dWRITE_SCRIPT_ANALYSIS, String string, long l, long[] lArray, int[] nArray, int n3, int n4, short[] sArray, short[] sArray2, short[] sArray3, short[] sArray4, int[] nArray2) {
        return OS.GetGlyphs(this.ptr, cArray, n, n2, iDWriteFontFace.ptr, bl, bl2, dWRITE_SCRIPT_ANALYSIS, string != null ? (string + "\u0000").toCharArray() : (char[])null, l, lArray, nArray, n3, n4, sArray, sArray2, sArray3, sArray4, nArray2);
    }

    int GetGlyphPlacements(char[] cArray, short[] sArray, short[] sArray2, int n, int n2, short[] sArray3, short[] sArray4, int n3, IDWriteFontFace iDWriteFontFace, float f, boolean bl, boolean bl2, DWRITE_SCRIPT_ANALYSIS dWRITE_SCRIPT_ANALYSIS, String string, long[] lArray, int[] nArray, int n4, float[] fArray, float[] fArray2) {
        return OS.GetGlyphPlacements(this.ptr, cArray, sArray, sArray2, n, n2, sArray3, sArray4, n3, iDWriteFontFace.ptr, f, bl, bl2, dWRITE_SCRIPT_ANALYSIS, string != null ? (string + "\u0000").toCharArray() : (char[])null, lArray, nArray, n4, fArray, fArray2);
    }
}

