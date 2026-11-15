/*
 * Decompiled with CFR 0.152.
 */
package com.sun.glass.ui;

import com.sun.glass.ui.View;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.tk.quantum.QuantumToolkit;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;

public abstract class Accessible {
    private EventHandler eventHandler;
    private View view;
    private GetAttribute getAttribute = new GetAttribute();
    private ExecuteAction executeAction = new ExecuteAction();

    public EventHandler getEventHandler() {
        return this.eventHandler;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return this.view;
    }

    public void dispose() {
        this.eventHandler = null;
        this.view = null;
    }

    public boolean isDisposed() {
        return this.getNativeAccessible() == 0L;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " (" + this.eventHandler + ")";
    }

    protected boolean isIgnored() {
        AccessibleRole accessibleRole = (AccessibleRole)((Object)this.getAttribute(AccessibleAttribute.ROLE, new Object[0]));
        if (accessibleRole == null) {
            return true;
        }
        return accessibleRole == AccessibleRole.NODE || accessibleRole == AccessibleRole.PARENT;
    }

    protected abstract long getNativeAccessible();

    protected Accessible getAccessible(Scene scene) {
        if (scene == null) {
            return null;
        }
        return SceneHelper.getAccessible(scene);
    }

    protected Accessible getAccessible(Node node) {
        if (node == null) {
            return null;
        }
        return NodeHelper.getAccessible(node);
    }

    protected long getNativeAccessible(Node node) {
        if (node == null) {
            return 0L;
        }
        Accessible accessible = this.getAccessible(node);
        if (accessible == null) {
            return 0L;
        }
        return accessible.getNativeAccessible();
    }

    protected Accessible getContainerAccessible(AccessibleRole accessibleRole) {
        Node node = (Node)this.getAttribute(AccessibleAttribute.PARENT, new Object[0]);
        while (node != null) {
            Accessible accessible = this.getAccessible(node);
            AccessibleRole accessibleRole2 = (AccessibleRole)((Object)accessible.getAttribute(AccessibleAttribute.ROLE, new Object[0]));
            if (accessibleRole2 == accessibleRole) {
                return accessible;
            }
            node = (Node)accessible.getAttribute(AccessibleAttribute.PARENT, new Object[0]);
        }
        return null;
    }

    private final AccessControlContext getAccessControlContext() {
        AccessControlContext accessControlContext = null;
        try {
            accessControlContext = this.eventHandler.getAccessControlContext();
        }
        catch (Exception exception) {
            // empty catch block
        }
        return accessControlContext;
    }

    public Object getAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        AccessControlContext accessControlContext = this.getAccessControlContext();
        if (accessControlContext == null) {
            return null;
        }
        return QuantumToolkit.runWithoutRenderLock(() -> {
            this.getAttribute.attribute = accessibleAttribute;
            this.getAttribute.parameters = objectArray;
            return AccessController.doPrivileged(this.getAttribute, accessControlContext);
        });
    }

    public void executeAction(AccessibleAction accessibleAction, Object ... objectArray) {
        AccessControlContext accessControlContext = this.getAccessControlContext();
        if (accessControlContext == null) {
            return;
        }
        QuantumToolkit.runWithoutRenderLock(() -> {
            this.executeAction.action = accessibleAction;
            this.executeAction.parameters = objectArray;
            return AccessController.doPrivileged(this.executeAction, accessControlContext);
        });
    }

    public abstract void sendNotification(AccessibleAttribute var1);

    private class ExecuteAction
    implements PrivilegedAction<Void> {
        AccessibleAction action;
        Object[] parameters;

        private ExecuteAction() {
        }

        @Override
        public Void run() {
            Accessible.this.eventHandler.executeAction(this.action, this.parameters);
            return null;
        }
    }

    private class GetAttribute
    implements PrivilegedAction<Object> {
        AccessibleAttribute attribute;
        Object[] parameters;

        private GetAttribute() {
        }

        @Override
        public Object run() {
            Class<?> clazz;
            Object object = Accessible.this.eventHandler.getAttribute(this.attribute, this.parameters);
            if (object != null && (clazz = this.attribute.getReturnType()) != null) {
                try {
                    clazz.cast(object);
                }
                catch (Exception exception) {
                    String string = "The expected return type for the " + this.attribute + " attribute is " + clazz.getSimpleName() + " but found " + object.getClass().getSimpleName();
                    System.err.println(string);
                    return null;
                }
            }
            return object;
        }
    }

    public static abstract class EventHandler {
        public Object getAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
            return null;
        }

        public void executeAction(AccessibleAction accessibleAction, Object ... objectArray) {
        }

        public abstract AccessControlContext getAccessControlContext();
    }
}

