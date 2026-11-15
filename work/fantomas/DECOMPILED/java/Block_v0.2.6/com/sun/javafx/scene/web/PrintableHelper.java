/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.web;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.web.Printable;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.util.Utils;
import javafx.scene.Node;

public class PrintableHelper
extends NodeHelper {
    private static final PrintableHelper theInstance = new PrintableHelper();
    private static PrintableAccessor printableAccessor;

    private static PrintableHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(Printable printable) {
        PrintableHelper.setHelper(printable, PrintableHelper.getInstance());
    }

    public static void setPrintableAccessor(PrintableAccessor printableAccessor) {
        if (PrintableHelper.printableAccessor != null) {
            throw new IllegalStateException();
        }
        PrintableHelper.printableAccessor = printableAccessor;
    }

    @Override
    protected NGNode createPeerImpl(Node node) {
        return printableAccessor.doCreatePeer(node);
    }

    @Override
    protected BaseBounds computeGeomBoundsImpl(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
        return printableAccessor.doComputeGeomBounds(node, baseBounds, baseTransform);
    }

    @Override
    protected boolean computeContainsImpl(Node node, double d, double d2) {
        return printableAccessor.doComputeContains(node, d, d2);
    }

    static {
        Utils.forceInit(Printable.class);
    }

    public static interface PrintableAccessor {
        public NGNode doCreatePeer(Node var1);

        public BaseBounds doComputeGeomBounds(Node var1, BaseBounds var2, BaseTransform var3);

        public boolean doComputeContains(Node var1, double var2, double var4);
    }
}

