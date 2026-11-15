/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javax.swing.SwingUtilities;

public class SwingNodeHelper
extends NodeHelper {
    private static final SwingNodeHelper theInstance = new SwingNodeHelper();
    private static SwingNodeAccessor swingNodeAccessor;
    private static final Set<Object> eventLoopKeys;

    private static SwingNodeHelper getInstance() {
        return theInstance;
    }

    public static void initHelper(SwingNode swingNode) {
        SwingNodeHelper.setHelper(swingNode, SwingNodeHelper.getInstance());
    }

    @Override
    protected NGNode createPeerImpl(Node node) {
        return swingNodeAccessor.doCreatePeer(node);
    }

    @Override
    protected void updatePeerImpl(Node node) {
        super.updatePeerImpl(node);
        swingNodeAccessor.doUpdatePeer(node);
    }

    @Override
    protected BaseBounds computeGeomBoundsImpl(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
        return swingNodeAccessor.doComputeGeomBounds(node, baseBounds, baseTransform);
    }

    @Override
    protected boolean computeContainsImpl(Node node, double d, double d2) {
        return swingNodeAccessor.doComputeContains(node, d, d2);
    }

    public static Object getLightweightFrame(SwingNode swingNode) {
        return swingNodeAccessor.getLightweightFrame(swingNode);
    }

    public static ReentrantLock getPaintLock(SwingNode swingNode) {
        return swingNodeAccessor.getPaintLock(swingNode);
    }

    public static void setImageBuffer(SwingNode swingNode, int[] nArray, int n, int n2, int n3, int n4, int n5, double d, double d2) {
        swingNodeAccessor.setImageBuffer(swingNode, nArray, n, n2, n3, n4, n5, d, d2);
    }

    public static void setImageBounds(SwingNode swingNode, int n, int n2, int n3, int n4) {
        swingNodeAccessor.setImageBounds(swingNode, n, n2, n3, n4);
    }

    public static void repaintDirtyRegion(SwingNode swingNode, int n, int n2, int n3, int n4) {
        swingNodeAccessor.repaintDirtyRegion(swingNode, n, n2, n3, n4);
    }

    public static void ungrabFocus(SwingNode swingNode, boolean bl) {
        swingNodeAccessor.ungrabFocus(swingNode, bl);
    }

    public static void setSwingPrefWidth(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingPrefWidth(swingNode, n);
    }

    public static void setSwingPrefHeight(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingPrefHeight(swingNode, n);
    }

    public static void setSwingMaxWidth(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingMaxWidth(swingNode, n);
    }

    public static void setSwingMaxHeight(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingMaxHeight(swingNode, n);
    }

    public static void setSwingMinWidth(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingMinWidth(swingNode, n);
    }

    public static void setSwingMinHeight(SwingNode swingNode, int n) {
        swingNodeAccessor.setSwingMinHeight(swingNode, n);
    }

    public static void setGrabbed(SwingNode swingNode, boolean bl) {
        swingNodeAccessor.setGrabbed(swingNode, bl);
    }

    public static void runOnFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void runOnEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public static void runOnEDTAndWait(Object object, Runnable runnable) {
        Toolkit.getToolkit().checkFxUserThread();
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            eventLoopKeys.add(object);
            SwingUtilities.invokeLater(runnable);
            Toolkit.getToolkit().enterNestedEventLoop(object);
        }
    }

    public static void leaveFXNestedLoop(Object object) {
        if (!eventLoopKeys.contains(object)) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            Toolkit.getToolkit().exitNestedEventLoop(object, null);
        } else {
            Platform.runLater(() -> Toolkit.getToolkit().exitNestedEventLoop(object, null));
        }
        eventLoopKeys.remove(object);
    }

    public static void setSwingNodeAccessor(SwingNodeAccessor swingNodeAccessor) {
        if (SwingNodeHelper.swingNodeAccessor != null) {
            throw new IllegalStateException();
        }
        SwingNodeHelper.swingNodeAccessor = swingNodeAccessor;
    }

    static {
        Utils.forceInit(SwingNode.class);
        eventLoopKeys = new HashSet<Object>();
    }

    public static interface SwingNodeAccessor {
        public NGNode doCreatePeer(Node var1);

        public void doUpdatePeer(Node var1);

        public BaseBounds doComputeGeomBounds(Node var1, BaseBounds var2, BaseTransform var3);

        public boolean doComputeContains(Node var1, double var2, double var4);

        public Object getLightweightFrame(SwingNode var1);

        public ReentrantLock getPaintLock(SwingNode var1);

        public void setImageBuffer(SwingNode var1, int[] var2, int var3, int var4, int var5, int var6, int var7, double var8, double var10);

        public void setImageBounds(SwingNode var1, int var2, int var3, int var4, int var5);

        public void repaintDirtyRegion(SwingNode var1, int var2, int var3, int var4, int var5);

        public void ungrabFocus(SwingNode var1, boolean var2);

        public void setSwingPrefWidth(SwingNode var1, int var2);

        public void setSwingPrefHeight(SwingNode var1, int var2);

        public void setSwingMaxWidth(SwingNode var1, int var2);

        public void setSwingMaxHeight(SwingNode var1, int var2);

        public void setSwingMinWidth(SwingNode var1, int var2);

        public void setSwingMinHeight(SwingNode var1, int var2);

        public void setGrabbed(SwingNode var1, boolean var2);
    }
}

