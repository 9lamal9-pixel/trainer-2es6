/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.media;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.util.Utils;
import javafx.scene.Node;
import javafx.scene.media.MediaView;

public class MediaViewHelper
extends NodeHelper {
    private static final MediaViewHelper theInstance = new MediaViewHelper();
    private static MediaViewAccessor mediaViewAccessor;

    private static MediaViewHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(MediaView mediaView) {
        MediaViewHelper.setHelper(mediaView, MediaViewHelper.getInstance());
    }

    @Override
    protected NGNode createPeerImpl(Node node) {
        return mediaViewAccessor.doCreatePeer(node);
    }

    @Override
    protected void updatePeerImpl(Node node) {
        super.updatePeerImpl(node);
        mediaViewAccessor.doUpdatePeer(node);
    }

    @Override
    protected void transformsChangedImpl(Node node) {
        super.transformsChangedImpl(node);
        mediaViewAccessor.doTransformsChanged(node);
    }

    @Override
    protected BaseBounds computeGeomBoundsImpl(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
        return mediaViewAccessor.doComputeGeomBounds(node, baseBounds, baseTransform);
    }

    @Override
    protected boolean computeContainsImpl(Node node, double d, double d2) {
        return mediaViewAccessor.doComputeContains(node, d, d2);
    }

    public static void setMediaViewAccessor(MediaViewAccessor mediaViewAccessor) {
        if (MediaViewHelper.mediaViewAccessor != null) {
            throw new IllegalStateException();
        }
        MediaViewHelper.mediaViewAccessor = mediaViewAccessor;
    }

    static {
        Utils.forceInit(MediaView.class);
    }

    public static interface MediaViewAccessor {
        public NGNode doCreatePeer(Node var1);

        public void doUpdatePeer(Node var1);

        public void doTransformsChanged(Node var1);

        public boolean doComputeContains(Node var1, double var2, double var4);

        public BaseBounds doComputeGeomBounds(Node var1, BaseBounds var2, BaseTransform var3);
    }
}

