/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.newimpl;

import com.sun.javafx.embed.swing.FXDnDInterop;
import com.sun.javafx.embed.swing.InteropFactory;
import com.sun.javafx.embed.swing.JFXPanelInterop;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import com.sun.javafx.embed.swing.SwingNodeInterop;
import com.sun.javafx.embed.swing.newimpl.FXDnDInteropN;
import com.sun.javafx.embed.swing.newimpl.JFXPanelInteropN;
import com.sun.javafx.embed.swing.newimpl.SwingFXUtilsImplInteropN;
import com.sun.javafx.embed.swing.newimpl.SwingNodeInteropN;

public class InteropFactoryN
extends InteropFactory {
    public InteropFactoryN() throws Exception {
        Class<?> clazz = Class.forName("jdk.swing.interop.LightweightFrameWrapper");
    }

    @Override
    public SwingNodeInterop createSwingNodeImpl() {
        return new SwingNodeInteropN();
    }

    @Override
    public JFXPanelInterop createJFXPanelImpl() {
        return new JFXPanelInteropN();
    }

    @Override
    public FXDnDInterop createFXDnDImpl() {
        return new FXDnDInteropN();
    }

    @Override
    public SwingFXUtilsImplInterop createSwingFXUtilsImpl() {
        return new SwingFXUtilsImplInteropN();
    }
}

