/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.oldimpl;

import com.sun.javafx.embed.swing.JFXPanelInterop;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import javafx.embed.swing.JFXPanel;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.UngrabEvent;

public class JFXPanelInteropO
extends JFXPanelInterop {
    @Override
    public void postEvent(JFXPanel jFXPanel, AWTEvent aWTEvent) {
        AppContext appContext = SunToolkit.targetToAppContext(jFXPanel);
        if (appContext != null) {
            SunToolkit.postEvent(appContext, aWTEvent);
        }
    }

    @Override
    public boolean isUngrabEvent(AWTEvent aWTEvent) {
        return aWTEvent instanceof UngrabEvent;
    }

    @Override
    public long getMask() {
        return -2147483632L;
    }

    @Override
    public void grab(Toolkit toolkit, Window window) {
        if (toolkit instanceof SunToolkit) {
            ((SunToolkit)toolkit).grab(window);
        }
    }

    @Override
    public void ungrab(Toolkit toolkit, Window window) {
        if (toolkit instanceof SunToolkit) {
            ((SunToolkit)toolkit).ungrab(window);
        }
    }
}

