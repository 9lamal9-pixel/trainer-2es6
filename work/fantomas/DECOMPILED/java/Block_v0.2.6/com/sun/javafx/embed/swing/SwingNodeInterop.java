/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.swing.DisposerRecord;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowFocusListener;
import javafx.embed.swing.SwingNode;
import javax.swing.JComponent;

public abstract class SwingNodeInterop {
    public abstract Object createLightweightFrame();

    public abstract Object getLightweightFrame();

    public abstract void disposeFrame(Object var1);

    public abstract MouseEvent createMouseEvent(Object var1, int var2, long var3, int var5, int var6, int var7, int var8, int var9, int var10, boolean var11, int var12);

    public abstract MouseWheelEvent createMouseWheelEvent(Object var1, int var2, int var3, int var4, int var5);

    public abstract KeyEvent createKeyEvent(Object var1, int var2, long var3, int var5, int var6, char var7);

    public abstract AWTEvent createUngrabEvent(Object var1);

    public abstract void overrideNativeWindowHandle(Object var1, long var2, Runnable var4);

    public abstract void addWindowFocusListener(Object var1, WindowFocusListener var2);

    public abstract void notifyDisplayChanged(Object var1, double var2, double var4);

    public abstract void setHostBounds(Object var1, int var2, int var3, int var4, int var5);

    public abstract void setContent(Object var1, Object var2);

    public abstract void setVisible(Object var1, boolean var2);

    public abstract void setBounds(Object var1, int var2, int var3, int var4, int var5);

    public abstract void emulateActivation(Object var1, boolean var2);

    public abstract Object createSwingNodeContent(JComponent var1, SwingNode var2);

    public abstract DisposerRecord createSwingNodeDisposer(Object var1);
}

