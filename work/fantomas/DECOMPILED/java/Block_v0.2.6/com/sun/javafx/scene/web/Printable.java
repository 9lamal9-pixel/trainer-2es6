/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.web;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.web.PrintableHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.prism.Graphics;
import com.sun.webkit.WebPage;
import com.sun.webkit.graphics.WCGraphicsContext;
import com.sun.webkit.graphics.WCGraphicsManager;
import javafx.scene.Node;

public final class Printable
extends Node {
    private final WebPage page;
    private final NGNode peer;

    public Printable(WebPage webPage, int n, float f) {
        this.page = webPage;
        this.peer = new Peer(n, f);
        PrintableHelper.initHelper(this);
    }

    private NGNode doCreatePeer() {
        return this.peer;
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        return false;
    }

    static {
        PrintableHelper.setPrintableAccessor(new PrintableHelper.PrintableAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Printable)node).doCreatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Printable)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Printable)node).doComputeContains(d, d2);
            }
        });
    }

    private final class Peer
    extends NGNode {
        private final int pageIndex;
        private final float width;

        Peer(int n, float f) {
            this.pageIndex = n;
            this.width = f;
        }

        @Override
        protected void renderContent(Graphics graphics) {
            WCGraphicsContext wCGraphicsContext = WCGraphicsManager.getGraphicsManager().createGraphicsContext(graphics);
            Printable.this.page.print(wCGraphicsContext, this.pageIndex, this.width);
        }

        @Override
        protected boolean hasOverlappingContents() {
            return false;
        }
    }
}

