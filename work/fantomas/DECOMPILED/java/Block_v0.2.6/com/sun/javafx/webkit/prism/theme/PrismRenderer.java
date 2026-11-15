/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism.theme;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.webkit.theme.Renderer;
import com.sun.prism.Graphics;
import com.sun.webkit.graphics.WCGraphicsContext;
import javafx.scene.control.Control;

public final class PrismRenderer
extends Renderer {
    @Override
    protected void render(Control control, WCGraphicsContext wCGraphicsContext) {
        SceneHelper.setAllowPGAccess(true);
        Object p = NodeHelper.getPeer(control);
        SceneHelper.setAllowPGAccess(false);
        ((NGNode)p).render((Graphics)wCGraphicsContext.getPlatformGraphics());
    }
}

