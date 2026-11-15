/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.theme;

import com.sun.javafx.webkit.theme.RenderThemeImpl;
import com.sun.javafx.webkit.theme.ScrollBarThemeImpl;
import com.sun.javafx.webkit.theme.ScrollBarWidgetHelper;
import com.sun.webkit.graphics.ScrollBarTheme;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;

public final class ScrollBarWidget
extends ScrollBar
implements RenderThemeImpl.Widget {
    private ScrollBarThemeImpl sbtImpl;
    private boolean thicknessInitialized;

    public ScrollBarWidget(ScrollBarThemeImpl scrollBarThemeImpl) {
        ScrollBarWidgetHelper.initHelper(this);
        this.thicknessInitialized = false;
        this.sbtImpl = scrollBarThemeImpl;
        this.setOrientation(Orientation.VERTICAL);
        this.setMin(0.0);
        this.setManaged(false);
    }

    private void doUpdatePeer() {
        this.initializeThickness();
    }

    @Override
    public RenderThemeImpl.WidgetType getType() {
        return RenderThemeImpl.WidgetType.SCROLLBAR;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.initializeThickness();
    }

    private void initializeThickness() {
        if (!this.thicknessInitialized) {
            ScrollBar scrollBar = this.sbtImpl.getTestSBRef();
            if (scrollBar == null) {
                return;
            }
            int n = (int)scrollBar.prefWidth(-1.0);
            if (n != 0 && ScrollBarTheme.getThickness() != n) {
                ScrollBarTheme.setThickness(n);
            }
            this.thicknessInitialized = true;
        }
    }

    static {
        ScrollBarWidgetHelper.setScrollBarWidgetAccessor(new ScrollBarWidgetHelper.ScrollBarWidgetAccessor(){

            @Override
            public void doUpdatePeer(Node node) {
                ((ScrollBarWidget)node).doUpdatePeer();
            }
        });
    }
}

