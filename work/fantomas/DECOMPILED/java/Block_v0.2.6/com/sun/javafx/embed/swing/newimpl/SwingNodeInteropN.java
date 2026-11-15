/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.newimpl;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.embed.swing.DisposerRecord;
import com.sun.javafx.embed.swing.FXDnD;
import com.sun.javafx.embed.swing.SwingCursors;
import com.sun.javafx.embed.swing.SwingNodeHelper;
import com.sun.javafx.embed.swing.SwingNodeInterop;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.util.Utils;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowFocusListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javax.swing.JComponent;
import jdk.swing.interop.DragSourceContextWrapper;
import jdk.swing.interop.LightweightContentWrapper;
import jdk.swing.interop.LightweightFrameWrapper;

public class SwingNodeInteropN
extends SwingNodeInterop {
    private volatile LightweightFrameWrapper lwFrame;
    private static OptionalMethod<LightweightFrameWrapper> jlfNotifyDisplayChanged;
    private static Class lwFrameWrapperClass;
    private static final OptionalMethod<LightweightFrameWrapper> jlfSetHostBounds;

    private static native void overrideNativeWindowHandle(Class var0, LightweightFrameWrapper var1, long var2, Runnable var4);

    @Override
    public LightweightFrameWrapper createLightweightFrame() {
        this.lwFrame = new LightweightFrameWrapper();
        return this.lwFrame;
    }

    @Override
    public LightweightFrameWrapper getLightweightFrame() {
        return this.lwFrame;
    }

    @Override
    public MouseEvent createMouseEvent(Object object, int n, long l, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, int n8) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        return lightweightFrameWrapper.createMouseEvent(lightweightFrameWrapper, n, l, n2, n3, n4, n5, n6, n7, bl, n8);
    }

    @Override
    public MouseWheelEvent createMouseWheelEvent(Object object, int n, int n2, int n3, int n4) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        return lightweightFrameWrapper.createMouseWheelEvent(lightweightFrameWrapper, n, n2, n3, n4);
    }

    @Override
    public KeyEvent createKeyEvent(Object object, int n, long l, int n2, int n3, char c) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        return lightweightFrameWrapper.createKeyEvent(lightweightFrameWrapper, n, l, n2, n3, c);
    }

    @Override
    public AWTEvent createUngrabEvent(Object object) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        return lightweightFrameWrapper.createUngrabEvent(lightweightFrameWrapper);
    }

    @Override
    public void overrideNativeWindowHandle(Object object, long l, Runnable runnable) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        SwingNodeInteropN.overrideNativeWindowHandle(lwFrameWrapperClass, lightweightFrameWrapper, l, runnable);
    }

    @Override
    public void notifyDisplayChanged(Object object, double d, double d2) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        if (jlfNotifyDisplayChanged.isIntegerApi()) {
            jlfNotifyDisplayChanged.invoke(lightweightFrameWrapper, (int)Math.round(d));
        } else {
            jlfNotifyDisplayChanged.invoke(lightweightFrameWrapper, d, d2);
        }
    }

    @Override
    public void setHostBounds(Object object, int n, int n2, int n3, int n4) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        jlfSetHostBounds.invoke(lightweightFrameWrapper, n, n2, n3, n4);
    }

    @Override
    public void setContent(Object object, Object object2) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        LightweightContentWrapper lightweightContentWrapper = (LightweightContentWrapper)object2;
        lightweightFrameWrapper.setContent(lightweightContentWrapper);
    }

    @Override
    public void setVisible(Object object, boolean bl) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        lightweightFrameWrapper.setVisible(bl);
    }

    @Override
    public void setBounds(Object object, int n, int n2, int n3, int n4) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        lightweightFrameWrapper.setBounds(n, n2, n3, n4);
    }

    @Override
    public LightweightContentWrapper createSwingNodeContent(JComponent jComponent, SwingNode swingNode) {
        return new SwingNodeContent(jComponent, swingNode);
    }

    @Override
    public DisposerRecord createSwingNodeDisposer(Object object) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        return new SwingNodeDisposer(lightweightFrameWrapper);
    }

    @Override
    public void emulateActivation(Object object, boolean bl) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        lightweightFrameWrapper.emulateActivation(bl);
    }

    @Override
    public void disposeFrame(Object object) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        lightweightFrameWrapper.dispose();
    }

    @Override
    public void addWindowFocusListener(Object object, WindowFocusListener windowFocusListener) {
        LightweightFrameWrapper lightweightFrameWrapper = (LightweightFrameWrapper)object;
        lightweightFrameWrapper.addWindowFocusListener(windowFocusListener);
    }

    static {
        lwFrameWrapperClass = null;
        jlfNotifyDisplayChanged = new OptionalMethod<LightweightFrameWrapper>(LightweightFrameWrapper.class, "notifyDisplayChanged", Double.TYPE, Double.TYPE);
        if (!jlfNotifyDisplayChanged.isSupported()) {
            jlfNotifyDisplayChanged = new OptionalMethod<LightweightFrameWrapper>(LightweightFrameWrapper.class, "notifyDisplayChanged", Integer.TYPE);
        }
        try {
            lwFrameWrapperClass = Class.forName("jdk.swing.interop.LightweightFrameWrapper");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        Utils.loadNativeSwingLibrary();
        jlfSetHostBounds = new OptionalMethod<LightweightFrameWrapper>(LightweightFrameWrapper.class, "setHostBounds", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
    }

    private static class SwingNodeContent
    extends LightweightContentWrapper {
        private JComponent comp;
        private volatile FXDnD dnd;
        private WeakReference<SwingNode> swingNodeRef;

        SwingNodeContent(JComponent jComponent, SwingNode swingNode) {
            this.comp = jComponent;
            this.swingNodeRef = new WeakReference<SwingNode>(swingNode);
        }

        @Override
        public JComponent getComponent() {
            return this.comp;
        }

        @Override
        public void paintLock() {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.getPaintLock(swingNode).lock();
            }
        }

        @Override
        public void paintUnlock() {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.getPaintLock(swingNode).unlock();
            }
        }

        @Override
        public void imageBufferReset(int[] nArray, int n, int n2, int n3, int n4, int n5) {
            this.imageBufferReset(nArray, n, n2, n3, n4, n5, 1);
        }

        public void imageBufferReset(int[] nArray, int n, int n2, int n3, int n4, int n5, int n6) {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.setImageBuffer(swingNode, nArray, n, n2, n3, n4, n5, n6, n6);
            }
        }

        @Override
        public void imageBufferReset(int[] nArray, int n, int n2, int n3, int n4, int n5, double d, double d2) {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.setImageBuffer(swingNode, nArray, n, n2, n3, n4, n5, d, d2);
            }
        }

        @Override
        public void imageReshaped(int n, int n2, int n3, int n4) {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.setImageBounds(swingNode, n, n2, n3, n4);
            }
        }

        @Override
        public void imageUpdated(int n, int n2, int n3, int n4) {
            SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
            if (swingNode != null) {
                SwingNodeHelper.repaintDirtyRegion(swingNode, n, n2, n3, n4);
            }
        }

        @Override
        public void focusGrabbed() {
            SwingNodeHelper.runOnFxThread(() -> {
                Scene scene;
                if (PlatformUtil.isLinux()) {
                    return;
                }
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null && (scene = swingNode.getScene()) != null && scene.getWindow() != null && WindowHelper.getPeer(scene.getWindow()) != null) {
                    WindowHelper.getPeer(scene.getWindow()).grabFocus();
                    SwingNodeHelper.setGrabbed(swingNode, true);
                }
            });
        }

        @Override
        public void focusUngrabbed() {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    Scene scene = swingNode.getScene();
                    SwingNodeHelper.ungrabFocus(swingNode, false);
                }
            });
        }

        @Override
        public void preferredSizeChanged(int n, int n2) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    SwingNodeHelper.setSwingPrefWidth(swingNode, n);
                    SwingNodeHelper.setSwingPrefHeight(swingNode, n2);
                    NodeHelper.notifyLayoutBoundsChanged(swingNode);
                }
            });
        }

        @Override
        public void maximumSizeChanged(int n, int n2) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    SwingNodeHelper.setSwingMaxWidth(swingNode, n);
                    SwingNodeHelper.setSwingMaxHeight(swingNode, n2);
                    NodeHelper.notifyLayoutBoundsChanged(swingNode);
                }
            });
        }

        @Override
        public void minimumSizeChanged(int n, int n2) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    SwingNodeHelper.setSwingMinWidth(swingNode, n);
                    SwingNodeHelper.setSwingMinHeight(swingNode, n2);
                    NodeHelper.notifyLayoutBoundsChanged(swingNode);
                }
            });
        }

        public void setCursor(Cursor cursor) {
            SwingNodeHelper.runOnFxThread(() -> {
                SwingNode swingNode = (SwingNode)this.swingNodeRef.get();
                if (swingNode != null) {
                    swingNode.setCursor(SwingCursors.embedCursorToCursor(cursor));
                }
            });
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void initDnD() {
            SwingNodeContent swingNodeContent = this;
            synchronized (swingNodeContent) {
                SwingNode swingNode;
                if (this.dnd == null && (swingNode = (SwingNode)this.swingNodeRef.get()) != null) {
                    this.dnd = new FXDnD(swingNode);
                }
            }
        }

        @Override
        public synchronized <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> clazz, DragSource dragSource, Component component, int n, DragGestureListener dragGestureListener) {
            this.initDnD();
            return this.dnd.createDragGestureRecognizer(clazz, dragSource, component, n, dragGestureListener);
        }

        @Override
        public DragSourceContextWrapper createDragSourceContext(DragGestureEvent dragGestureEvent) throws InvalidDnDOperationException {
            this.initDnD();
            return (DragSourceContextWrapper)this.dnd.createDragSourceContext(dragGestureEvent);
        }

        @Override
        public void addDropTarget(DropTarget dropTarget) {
            this.initDnD();
            this.dnd.addDropTarget(dropTarget);
        }

        @Override
        public void removeDropTarget(DropTarget dropTarget) {
            this.initDnD();
            this.dnd.removeDropTarget(dropTarget);
        }
    }

    private static class SwingNodeDisposer
    implements DisposerRecord {
        LightweightFrameWrapper lwFrame;

        SwingNodeDisposer(LightweightFrameWrapper lightweightFrameWrapper) {
            this.lwFrame = lightweightFrameWrapper;
        }

        @Override
        public void dispose() {
            if (this.lwFrame != null) {
                this.lwFrame.dispose();
                this.lwFrame = null;
            }
        }
    }

    private static final class OptionalMethod<T> {
        private final Method method;
        private final boolean isIntegerAPI;

        OptionalMethod(Class<T> clazz, String string, Class<?> ... classArray) {
            Method method;
            try {
                method = clazz.getMethod(string, classArray);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                method = null;
            }
            catch (Throwable throwable) {
                throw new RuntimeException("Error when calling " + clazz.getName() + ".getMethod('" + string + "').", throwable);
            }
            this.method = method;
            this.isIntegerAPI = classArray != null && classArray.length > 0 && classArray[0] == Integer.TYPE;
        }

        public boolean isSupported() {
            return this.method != null;
        }

        public boolean isIntegerApi() {
            return this.isIntegerAPI;
        }

        public Object invoke(T t, Object ... objectArray) {
            if (this.method != null) {
                try {
                    return this.method.invoke(t, objectArray);
                }
                catch (Throwable throwable) {
                    throw new RuntimeException("Error when calling " + t.getClass().getName() + "." + this.method.getName() + "().", throwable);
                }
            }
            return null;
        }
    }
}

