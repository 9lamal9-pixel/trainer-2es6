/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.webkit.ContextMenu;
import com.sun.webkit.MethodHelper;
import com.sun.webkit.Pasteboard;
import com.sun.webkit.PopupMenu;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;

public abstract class Utilities {
    private static Utilities instance;

    public static synchronized void setUtilities(Utilities utilities) {
        instance = utilities;
    }

    public static synchronized Utilities getUtilities() {
        return instance;
    }

    protected abstract Pasteboard createPasteboard();

    protected abstract PopupMenu createPopupMenu();

    protected abstract ContextMenu createContextMenu();

    private static Object fwkInvokeWithContext(Method method, Object object, Object[] objectArray, AccessControlContext accessControlContext) throws Throwable {
        try {
            return AccessController.doPrivileged(() -> MethodHelper.invoke(method, object, objectArray), accessControlContext);
        }
        catch (PrivilegedActionException privilegedActionException) {
            Throwable throwable = privilegedActionException.getCause();
            if (throwable == null) {
                throwable = privilegedActionException;
            } else if (throwable instanceof InvocationTargetException && throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
            throw throwable;
        }
    }
}

