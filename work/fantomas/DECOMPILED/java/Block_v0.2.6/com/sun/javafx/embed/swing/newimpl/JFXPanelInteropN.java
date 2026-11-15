/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.newimpl;

import com.sun.javafx.embed.swing.JFXPanelInterop;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import javafx.embed.swing.JFXPanel;
import jdk.swing.interop.SwingInterOpUtils;

public class JFXPanelInteropN
extends JFXPanelInterop {
    @Override
    public void postEvent(JFXPanel jFXPanel, AWTEvent aWTEvent) {
        SwingInterOpUtils.postEvent(jFXPanel, aWTEvent);
    }

    @Override
    public boolean isUngrabEvent(AWTEvent aWTEvent) {
        return SwingInterOpUtils.isUngrabEvent(aWTEvent);
    }

    @Override
    public long getMask() {
        return -2147483632L;
    }

    @Override
    public void grab(Toolkit toolkit, Window window) {
        SwingInterOpUtils.grab(toolkit, window);
    }

    @Override
    public void ungrab(Toolkit toolkit, Window window) {
        SwingInterOpUtils.ungrab(toolkit, window);
    }
}

