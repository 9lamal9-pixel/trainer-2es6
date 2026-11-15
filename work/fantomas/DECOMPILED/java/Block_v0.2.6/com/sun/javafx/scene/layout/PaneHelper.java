/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.layout;

import com.sun.javafx.scene.layout.RegionHelper;
import com.sun.javafx.util.Utils;
import javafx.scene.layout.Pane;

public class PaneHelper
extends RegionHelper {
    private static final PaneHelper theInstance = new PaneHelper();
    private static PaneAccessor paneAccessor;

    private static PaneHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(Pane pane) {
        PaneHelper.setHelper(pane, PaneHelper.getInstance());
    }

    public static void setPaneAccessor(PaneAccessor paneAccessor) {
        if (PaneHelper.paneAccessor != null) {
            throw new IllegalStateException();
        }
        PaneHelper.paneAccessor = paneAccessor;
    }

    static {
        Utils.forceInit(Pane.class);
    }

    public static interface PaneAccessor {
    }
}

