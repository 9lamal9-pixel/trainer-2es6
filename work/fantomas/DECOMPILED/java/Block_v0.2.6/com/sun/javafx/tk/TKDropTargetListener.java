/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.tk;

import com.sun.javafx.tk.TKClipboard;
import javafx.scene.input.TransferMode;

public interface TKDropTargetListener {
    public TransferMode dragEnter(double var1, double var3, double var5, double var7, TransferMode var9, TKClipboard var10);

    public TransferMode dragOver(double var1, double var3, double var5, double var7, TransferMode var9);

    public void dragExit(double var1, double var3, double var5, double var7);

    public TransferMode drop(double var1, double var3, double var5, double var7, TransferMode var9);
}

