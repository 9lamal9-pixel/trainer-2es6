/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.oldimpl;

import com.sun.javafx.embed.swing.FXDnDInterop;
import com.sun.javafx.embed.swing.InteropFactory;
import com.sun.javafx.embed.swing.JFXPanelInterop;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import com.sun.javafx.embed.swing.SwingNodeInterop;
import com.sun.javafx.embed.swing.oldimpl.FXDnDInteropO;
import com.sun.javafx.embed.swing.oldimpl.JFXPanelInteropO;
import com.sun.javafx.embed.swing.oldimpl.SwingFXUtilsImplInteropO;
import com.sun.javafx.embed.swing.oldimpl.SwingNodeInteropO;

public class InteropFactoryO
extends InteropFactory {
    public InteropFactoryO() throws Exception {
        Class<?> clazz = Class.forName("sun.swing.JLightweightFrame");
    }

    @Override
    public SwingNodeInterop createSwingNodeImpl() {
        return new SwingNodeInteropO();
    }

    @Override
    public JFXPanelInterop createJFXPanelImpl() {
        return new JFXPanelInteropO();
    }

    @Override
    public FXDnDInterop createFXDnDImpl() {
        return new FXDnDInteropO();
    }

    @Override
    public SwingFXUtilsImplInterop createSwingFXUtilsImpl() {
        return new SwingFXUtilsImplInteropO();
    }
}

