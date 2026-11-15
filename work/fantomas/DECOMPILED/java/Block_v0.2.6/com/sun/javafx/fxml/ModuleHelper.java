/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml;

import com.sun.javafx.fxml.MethodHelper;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

public class ModuleHelper {
    private static final Method getModuleMethod;
    private static final Method getResourceAsStreamMethod;
    private static final boolean verbose;

    public static Object getModule(Class clazz) {
        if (getModuleMethod != null) {
            try {
                return getModuleMethod.invoke((Object)clazz, new Object[0]);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        }
        return null;
    }

    public static InputStream getResourceAsStream(Object object, String string) {
        if (getResourceAsStreamMethod != null) {
            try {
                return (InputStream)getResourceAsStreamMethod.invoke(object, string);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        }
        return null;
    }

    public static Object invoke(Method method, Object object, Object[] objectArray) throws InvocationTargetException, IllegalAccessException {
        Object object2 = ModuleHelper.getModule(ModuleHelper.class);
        Object object3 = ModuleHelper.getModule(method.getDeclaringClass());
        if (verbose) {
            System.out.println("thisModule = " + object2);
            System.out.println("methodModule = " + object3);
            System.out.println("m = " + method);
        }
        if (object3 != object2) {
            return MethodHelper.invoke(method, object, objectArray);
        }
        return method.invoke(object, objectArray);
    }

    static {
        verbose = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.verbose"));
        if (verbose) {
            System.err.println(ModuleHelper.class.getName() + " : <clinit>");
        }
        Method method = null;
        Method method2 = null;
        try {
            method = Class.class.getMethod("getModule", new Class[0]);
            Class<?> clazz = method.getReturnType();
            method2 = clazz.getMethod("getResourceAsStream", String.class);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
        getModuleMethod = method;
        getResourceAsStreamMethod = method2;
        if (verbose) {
            System.err.println("getModuleMethod = " + getModuleMethod);
            System.err.println("getResourceAsStreamMethod = " + getResourceAsStreamMethod);
        }
    }
}

