/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.theme;

import com.sun.javafx.scene.control.ControlHelper;
import com.sun.javafx.util.Utils;
import com.sun.javafx.webkit.theme.ScrollBarWidget;
import javafx.scene.Node;

public class ScrollBarWidgetHelper
extends ControlHelper {
    private static final ScrollBarWidgetHelper theInstance = new ScrollBarWidgetHelper();
    private static ScrollBarWidgetAccessor scrollBarWidgetAccessor;

    private static ScrollBarWidgetHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(ScrollBarWidget scrollBarWidget) {
        ScrollBarWidgetHelper.setHelper(scrollBarWidget, ScrollBarWidgetHelper.getInstance());
    }

    @Override
    protected void updatePeerImpl(Node node) {
        super.updatePeerImpl(node);
        scrollBarWidgetAccessor.doUpdatePeer(node);
    }

    public static void setScrollBarWidgetAccessor(ScrollBarWidgetAccessor scrollBarWidgetAccessor) {
        if (ScrollBarWidgetHelper.scrollBarWidgetAccessor != null) {
            throw new IllegalStateException();
        }
        ScrollBarWidgetHelper.scrollBarWidgetAccessor = scrollBarWidgetAccessor;
    }

    static {
        Utils.forceInit(ScrollBarWidget.class);
    }

    public static interface ScrollBarWidgetAccessor {
        public void doUpdatePeer(Node var1);
    }
}

