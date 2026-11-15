/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import javafx.embed.swing.JFXPanel;

public abstract class JFXPanelInterop {
    public abstract void postEvent(JFXPanel var1, AWTEvent var2);

    public abstract boolean isUngrabEvent(AWTEvent var1);

    public abstract long getMask();

    public abstract void grab(Toolkit var1, Window var2);

    public abstract void ungrab(Toolkit var1, Window var2);
}

