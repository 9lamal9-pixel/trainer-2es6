/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.font.directwrite;

import com.sun.javafx.font.directwrite.IUnknown;
import com.sun.javafx.font.directwrite.OS;

class IDWriteFontFile
extends IUnknown {
    IDWriteFontFile(long l) {
        super(l);
    }

    int Analyze(boolean[] blArray, int[] nArray, int[] nArray2, int[] nArray3) {
        return OS.Analyze(this.ptr, blArray, nArray, nArray2, nArray3);
    }
}

