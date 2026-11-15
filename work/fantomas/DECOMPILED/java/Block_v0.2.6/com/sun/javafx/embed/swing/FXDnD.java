/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.swing.FXDnDInterop;
import com.sun.javafx.embed.swing.InteropFactory;
import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javafx.embed.swing.SwingNode;

public final class FXDnD {
    private static SwingNode node;
    public static boolean fxAppThreadIsDispatchThread;
    private static FXDnDInterop fxdndiop;

    private SwingNode getNode() {
        return node;
    }

    public FXDnD(SwingNode swingNode) {
        node = swingNode;
        fxdndiop.setNode(swingNode);
    }

    public Object createDragSourceContext(DragGestureEvent dragGestureEvent) throws InvalidDnDOperationException {
        return fxdndiop.createDragSourceContext(dragGestureEvent);
    }

    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> clazz, DragSource dragSource, Component component, int n, DragGestureListener dragGestureListener) {
        return fxdndiop.createDragGestureRecognizer(dragSource, component, n, dragGestureListener);
    }

    public void addDropTarget(DropTarget dropTarget) {
        fxdndiop.addDropTarget(dropTarget, node);
    }

    public void removeDropTarget(DropTarget dropTarget) {
        fxdndiop.removeDropTarget(dropTarget, node);
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                fxAppThreadIsDispatchThread = "true".equals(System.getProperty("javafx.embed.singleThread"));
                return null;
            }
        });
        InteropFactory interopFactory = null;
        try {
            interopFactory = InteropFactory.getInstance();
        }
        catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
        fxdndiop = interopFactory.createFXDnDImpl();
    }
}

