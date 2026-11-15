/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.theme;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.webkit.Accessor;
import com.sun.javafx.webkit.theme.RenderThemeImpl;
import com.sun.javafx.webkit.theme.Renderer;
import com.sun.javafx.webkit.theme.ScrollBarWidget;
import com.sun.webkit.graphics.Ref;
import com.sun.webkit.graphics.ScrollBarTheme;
import com.sun.webkit.graphics.WCGraphicsContext;
import com.sun.webkit.graphics.WCSize;
import java.lang.ref.WeakReference;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;

public final class ScrollBarThemeImpl
extends ScrollBarTheme {
    private static final PlatformLogger log = PlatformLogger.getLogger(ScrollBarThemeImpl.class.getName());
    private WeakReference<ScrollBar> testSBRef = new WeakReference<Object>(null);
    private final Accessor accessor;
    private final RenderThemeImpl.Pool<ScrollBarWidget> pool;

    public ScrollBarThemeImpl(final Accessor accessor) {
        this.accessor = accessor;
        this.pool = new RenderThemeImpl.Pool<ScrollBarWidget>(scrollBarWidget -> accessor.removeChild((Node)scrollBarWidget), ScrollBarWidget.class);
        accessor.addViewListener(new RenderThemeImpl.ViewListener(this.pool, accessor){

            @Override
            public void invalidated(Observable observable) {
                super.invalidated(observable);
                ScrollBarWidget scrollBarWidget = new ScrollBarWidget(ScrollBarThemeImpl.this);
                accessor.addChild(scrollBarWidget);
                ScrollBarThemeImpl.this.testSBRef = new WeakReference<ScrollBarWidget>(scrollBarWidget);
            }
        });
    }

    ScrollBar getTestSBRef() {
        return (ScrollBar)this.testSBRef.get();
    }

    private static Orientation convertOrientation(int n) {
        return n == 1 ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }

    private void adjustScrollBar(ScrollBar scrollBar, int n, int n2, int n3) {
        Orientation orientation = ScrollBarThemeImpl.convertOrientation(n3);
        if (orientation != scrollBar.getOrientation()) {
            scrollBar.setOrientation(orientation);
        }
        if (orientation == Orientation.VERTICAL) {
            n = ScrollBarTheme.getThickness();
        } else {
            n2 = ScrollBarTheme.getThickness();
        }
        if ((double)n != scrollBar.getWidth() || (double)n2 != scrollBar.getHeight()) {
            scrollBar.resize(n, n2);
        }
    }

    private void adjustScrollBar(ScrollBar scrollBar, int n, int n2, int n3, int n4, int n5, int n6) {
        this.adjustScrollBar(scrollBar, n, n2, n3);
        boolean bl = n6 <= n5;
        scrollBar.setDisable(bl);
        if (bl) {
            return;
        }
        if (n4 < 0) {
            n4 = 0;
        } else if (n4 > n6 - n5) {
            n4 = n6 - n5;
        }
        if (scrollBar.getMax() != (double)n6 || scrollBar.getVisibleAmount() != (double)n5) {
            scrollBar.setValue(0.0);
            scrollBar.setMax(n6);
            scrollBar.setVisibleAmount(n5);
        }
        if (n6 > n5) {
            float f = (float)n6 / (float)(n6 - n5);
            if (scrollBar.getValue() != (double)((float)n4 * f)) {
                scrollBar.setValue((float)n4 * f);
            }
        }
    }

    @Override
    protected Ref createWidget(long l, int n, int n2, int n3, int n4, int n5, int n6) {
        ScrollBarWidget scrollBarWidget = this.pool.get(l);
        if (scrollBarWidget == null) {
            scrollBarWidget = new ScrollBarWidget(this);
            this.pool.put(l, scrollBarWidget, this.accessor.getPage().getUpdateContentCycleID());
            this.accessor.addChild(scrollBarWidget);
        }
        this.adjustScrollBar(scrollBarWidget, n, n2, n3, n4, n5, n6);
        return new ScrollBarRef(scrollBarWidget);
    }

    @Override
    public void paint(WCGraphicsContext wCGraphicsContext, Ref ref, int n, int n2, int n3, int n4) {
        ScrollBar scrollBar = (ScrollBar)((ScrollBarRef)ref).asControl();
        if (scrollBar == null) {
            return;
        }
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            log.finest("[{0}, {1} {2}x{3}], {4}", n, n2, scrollBar.getWidth(), scrollBar.getHeight(), scrollBar.getOrientation() == Orientation.VERTICAL ? "VERTICAL" : "HORIZONTAL");
        }
        wCGraphicsContext.saveState();
        wCGraphicsContext.translate(n, n2);
        Renderer.getRenderer().render(scrollBar, wCGraphicsContext);
        wCGraphicsContext.restoreState();
    }

    @Override
    public WCSize getWidgetSize(Ref ref) {
        ScrollBar scrollBar = (ScrollBar)((ScrollBarRef)ref).asControl();
        if (scrollBar != null) {
            return new WCSize((float)scrollBar.getWidth(), (float)scrollBar.getHeight());
        }
        return new WCSize(0.0f, 0.0f);
    }

    @Override
    protected void getScrollBarPartRect(long l, int n, int[] nArray) {
        ScrollBar scrollBar = this.pool.get(l);
        if (scrollBar == null) {
            return;
        }
        Node node = null;
        if (n == 2) {
            node = ScrollBarThemeImpl.getIncButton(scrollBar);
        } else if (n == 1) {
            node = ScrollBarThemeImpl.getDecButton(scrollBar);
        } else if (n == 256) {
            node = ScrollBarThemeImpl.getTrack(scrollBar);
        }
        assert (nArray.length >= 4);
        if (node != null) {
            Bounds bounds = node.getBoundsInParent();
            nArray[0] = (int)bounds.getMinX();
            nArray[1] = (int)bounds.getMinY();
            nArray[2] = (int)bounds.getWidth();
            nArray[3] = (int)bounds.getHeight();
        } else {
            nArray[3] = 0;
            nArray[2] = 0;
            nArray[1] = 0;
            nArray[0] = 0;
        }
        log.finest("id {0} part {1} bounds {2},{3} {4}x{5}", String.valueOf(l), String.valueOf(n), nArray[0], nArray[1], nArray[2], nArray[3]);
    }

    private static Node getTrack(ScrollBar scrollBar) {
        return ScrollBarThemeImpl.findNode(scrollBar, "track");
    }

    private static Node getIncButton(ScrollBar scrollBar) {
        return ScrollBarThemeImpl.findNode(scrollBar, "increment-button");
    }

    private static Node getDecButton(ScrollBar scrollBar) {
        return ScrollBarThemeImpl.findNode(scrollBar, "decrement-button");
    }

    private static Node findNode(ScrollBar scrollBar, String string) {
        for (Node node : scrollBar.getChildrenUnmodifiable()) {
            if (!node.getStyleClass().contains(string)) continue;
            return node;
        }
        return null;
    }

    private static final class ScrollBarRef
    extends Ref {
        private final WeakReference<ScrollBarWidget> sbRef;

        private ScrollBarRef(ScrollBarWidget scrollBarWidget) {
            this.sbRef = new WeakReference<ScrollBarWidget>(scrollBarWidget);
        }

        private Control asControl() {
            return (Control)this.sbRef.get();
        }
    }
}

