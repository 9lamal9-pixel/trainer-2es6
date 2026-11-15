/*
 * Decompiled with CFR 0.152.
 */
package com.sun.java.scene.web;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.util.Utils;
import javafx.scene.Node;
import javafx.scene.web.WebView;

public class WebViewHelper
extends ParentHelper {
    private static final WebViewHelper theInstance = new WebViewHelper();
    private static WebViewAccessor webViewAccessor;

    private static WebViewHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(WebView webView) {
        WebViewHelper.setHelper(webView, WebViewHelper.getInstance());
    }

    @Override
    protected NGNode createPeerImpl(Node node) {
        return webViewAccessor.doCreatePeer(node);
    }

    @Override
    protected void updatePeerImpl(Node node) {
        super.updatePeerImpl(node);
        webViewAccessor.doUpdatePeer(node);
    }

    @Override
    protected void transformsChangedImpl(Node node) {
        super.transformsChangedImpl(node);
        webViewAccessor.doTransformsChanged(node);
    }

    @Override
    protected BaseBounds computeGeomBoundsImpl(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
        return webViewAccessor.doComputeGeomBounds(node, baseBounds, baseTransform);
    }

    @Override
    protected boolean computeContainsImpl(Node node, double d, double d2) {
        return webViewAccessor.doComputeContains(node, d, d2);
    }

    @Override
    protected void pickNodeLocalImpl(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
        webViewAccessor.doPickNodeLocal(node, pickRay, pickResultChooser);
    }

    public static void setWebViewAccessor(WebViewAccessor webViewAccessor) {
        if (WebViewHelper.webViewAccessor != null) {
            throw new IllegalStateException();
        }
        WebViewHelper.webViewAccessor = webViewAccessor;
    }

    static {
        Utils.forceInit(WebView.class);
    }

    public static interface WebViewAccessor {
        public NGNode doCreatePeer(Node var1);

        public void doUpdatePeer(Node var1);

        public void doTransformsChanged(Node var1);

        public BaseBounds doComputeGeomBounds(Node var1, BaseBounds var2, BaseTransform var3);

        public boolean doComputeContains(Node var1, double var2, double var4);

        public void doPickNodeLocal(Node var1, PickRay var2, PickResultChooser var3);
    }
}

