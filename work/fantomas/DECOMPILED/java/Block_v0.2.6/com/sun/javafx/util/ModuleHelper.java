/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

public class ModuleHelper {
    private static final Method getModuleMethod;
    private static final Method addReadsMethod;
    private static final Method addExportsMethod;
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

    public static void addReads(Object object, Object object2) {
        if (addReadsMethod != null) {
            try {
                addReadsMethod.invoke(object, object2);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        }
    }

    public static void addExports(Object object, String string, Object object2) {
        if (addExportsMethod != null) {
            try {
                addExportsMethod.invoke(object, string, object2);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        }
    }

    static {
        verbose = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.verbose"));
        if (verbose) {
            System.err.println(ModuleHelper.class.getName() + " : <clinit>");
        }
        Method method = null;
        Method method2 = null;
        Method method3 = null;
        try {
            method = Class.class.getMethod("getModule", new Class[0]);
            Class<?> clazz = method.getReturnType();
            method2 = clazz.getMethod("addReads", clazz);
            method3 = clazz.getMethod("addExports", String.class, clazz);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
        getModuleMethod = method;
        addReadsMethod = method2;
        addExportsMethod = method3;
        if (verbose) {
            System.err.println("getModuleMethod = " + getModuleMethod);
            System.err.println("addReadsMethod = " + addReadsMethod);
            System.err.println("addExportsMethod = " + addExportsMethod);
        }
    }
}

